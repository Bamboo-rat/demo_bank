package com.example.loanservice.service.impl;

import com.example.loanservice.client.CoreBankingClient;
import com.example.loanservice.client.CustomerServiceClient;
import com.example.loanservice.dto.corebanking.CoreLoanRepaymentRequest;
import com.example.loanservice.dto.corebanking.CoreLoanRepaymentResponse;
import com.example.loanservice.dto.corebanking.CoreLoanDisbursementResponse;
import com.example.loanservice.dto.customer.CustomerInfoResponse;
import com.example.loanservice.dto.request.DisbursementRequest;
import com.example.loanservice.dto.request.EarlySettlementRequest;
import com.example.loanservice.dto.request.RepaymentRequest;
import com.example.loanservice.dto.response.DisbursementResponse;
import com.example.loanservice.dto.response.LoanPaymentHistoryResponse;
import com.example.loanservice.dto.response.RepaymentResponse;
import com.example.loanservice.entity.LoanAccount;
import com.example.loanservice.entity.LoanPaymentHistory;
import com.example.loanservice.entity.RepaymentSchedule;
import com.example.loanservice.entity.enums.InstallmentStatus;
import com.example.loanservice.entity.enums.LoanStatus;
import com.example.loanservice.event.*;
import com.example.loanservice.exception.ErrorCode;
import com.example.loanservice.exception.LoanServiceException;
import com.example.loanservice.mapper.LoanPaymentHistoryMapper;
import com.example.loanservice.repository.LoanAccountRepository;
import com.example.loanservice.repository.LoanPaymentHistoryRepository;
import com.example.loanservice.repository.RepaymentScheduleRepository;
import com.example.loanservice.service.RepaymentScheduleService;
import com.example.loanservice.service.RepaymentService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepaymentServiceImpl implements RepaymentService {
    
    private final LoanAccountRepository accountRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanPaymentHistoryRepository paymentHistoryRepository;
    private final CoreBankingClient coreBankingClient;
    private final CustomerServiceClient customerServiceClient;
    private final RepaymentScheduleService scheduleService;
    private final LoanEventProducer eventProducer;
    private final LoanPaymentHistoryMapper paymentHistoryMapper;
    
    @Override
    @Transactional
    public DisbursementResponse disburseLoan(DisbursementRequest request) {
        log.info("[DISB-001] Disbursing loan: {}", request.getLoanAccountId());
        
        LoanAccount loanAccount = accountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, 
                "Loan ID: " + request.getLoanAccountId()));
        
        if (loanAccount.getStatus() != LoanStatus.APPROVED) {
            log.error("[DISB-002] Loan not in approved status: {}", loanAccount.getStatus());
            throw new LoanServiceException(ErrorCode.ACC_003, "Loan status: " + loanAccount.getStatus());
        }
        
        // Call Core Banking to disburse
        CoreLoanDisbursementResponse coreResponse;
        try {
            coreResponse = coreBankingClient.disburseLoan(loanAccount.getLoanId());
            log.info("[DISB-004] Core Banking disbursement successful. TxId: {}", coreResponse.getTransactionId());
            
        } catch (FeignException e) {
            log.error("[DISB-005] Core Banking disbursement failed", e);
            throw new LoanServiceException(ErrorCode.CORE_003, "Disbursement failed in Core Banking", e);
        }
        
        // Update loan account
        loanAccount.setOutstandingPrincipal(loanAccount.getApprovedAmount());
        loanAccount.setStatus(LoanStatus.ACTIVE);
        loanAccount.setDisbursementTxRef(coreResponse.getTransactionId());
        loanAccount.setDisbursementDate(coreResponse.getDisbursementTime() != null ? coreResponse.getDisbursementTime() : LocalDateTime.now());
        accountRepository.save(loanAccount);
        
        log.info("[DISB-006] Loan account updated to ACTIVE");
        
        // Get customer info for event
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfo(loanAccount.getCustomerId());
        
        // Publish event
        LoanDisbursedEvent event = LoanDisbursedEvent.builder()
            .loanAccountId(loanAccount.getLoanId())
                .cifId(loanAccount.getCustomerId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .disbursedAmount(loanAccount.getApprovedAmount())
                .accountId(loanAccount.getDisbursementAccount())
                .transactionId(coreResponse.getTransactionId())
                .disbursementTime(coreResponse.getDisbursementTime())
                .eventTime(LocalDateTime.now())
                .build();
        
        eventProducer.publishLoanDisbursedEvent(event);
        log.info("[DISB-007] Disbursement completed successfully");
        
        return DisbursementResponse.builder()
            .loanAccountId(loanAccount.getLoanId())
                .transactionId(coreResponse.getTransactionId())
                .disbursedAmount(loanAccount.getApprovedAmount())
                .accountId(loanAccount.getDisbursementAccount())
                .disbursementTime(coreResponse.getDisbursementTime())
                .status("SUCCESS")
                .message("Loan disbursed successfully")
                .build();
    }
    
    @Override
    @Transactional
    public RepaymentResponse repayInstallment(RepaymentRequest request) {
        log.info("[REPAY-001] Processing repayment for loan: {}", request.getLoanAccountId());
        
        LoanAccount loanAccount = accountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, 
                "Loan ID: " + request.getLoanAccountId()));
        
        if (loanAccount.getStatus() != LoanStatus.ACTIVE && loanAccount.getStatus() != LoanStatus.OVERDUE) {
            log.error("[REPAY-002] Loan not active or overdue: {}", loanAccount.getStatus());
            throw new LoanServiceException(ErrorCode.ACC_003, "Loan status: " + loanAccount.getStatus());
        }
        
        // Get installment to pay
        RepaymentSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new LoanServiceException(ErrorCode.SCH_001, 
                        "Schedule ID: " + request.getScheduleId()));
        
        if (schedule.getStatus() == InstallmentStatus.PAID) {
            log.error("[REPAY-003] Installment already paid");
            throw new LoanServiceException(ErrorCode.SCH_006, "Installment already paid");
        }
        
        // Prepare Core Banking request
        CoreLoanRepaymentRequest coreRequest = CoreLoanRepaymentRequest.builder()
            .loanServiceRef(loanAccount.getLoanId())
                .accountId(loanAccount.getRepaymentAccount())
                .amount(schedule.getTotalAmount())
                .principalAmount(schedule.getPrincipalAmount())
                .interestAmount(schedule.getInterestAmount())
                .penaltyAmount(schedule.getPenaltyAmount())
                .scheduleRef(request.getScheduleId())
                .notes("Installment payment #" + schedule.getInstallmentNo())
                .build();
        
        // Call Core Banking to debit money
        CoreLoanRepaymentResponse coreResponse;
        try {
            coreResponse = coreBankingClient.repayLoan(coreRequest);
            log.info("[REPAY-004] Core Banking repayment successful. TxId: {}", coreResponse.getTransactionId());
            
        } catch (FeignException e) {
            log.error("[REPAY-005] Core Banking repayment failed", e);
            throw new LoanServiceException(ErrorCode.CORE_004, "Repayment failed in Core Banking", e);
        }
        
        // Update installment status
        schedule.setStatus(InstallmentStatus.PAID);
        schedule.setPaidAmount(coreResponse.getPaidAmount());
        schedule.setPaidDate(coreResponse.getRepaymentTime() != null ? coreResponse.getRepaymentTime() : LocalDateTime.now());
        schedule.setPaymentTxRef(coreResponse.getTransactionId());
        scheduleRepository.save(schedule);
        
        // Update loan account outstanding
        loanAccount.setOutstandingPrincipal(coreResponse.getOutstandingPrincipal());
        
        if (coreResponse.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) == 0) {
            loanAccount.setStatus(LoanStatus.CLOSED);
            log.info("[REPAY-006] Loan fully paid, status set to CLOSED");
        } else if (loanAccount.getStatus() == LoanStatus.OVERDUE) {
            loanAccount.setStatus(LoanStatus.ACTIVE);
            log.info("[REPAY-007] Overdue loan paid, status reset to ACTIVE");
        }
        
        accountRepository.save(loanAccount);
        
        // Create payment history
        LoanPaymentHistory paymentHistory = LoanPaymentHistory.builder()
            .loanId(loanAccount.getLoanId())
                .scheduleId(schedule.getScheduleId())
                .paidAmount(coreResponse.getPaidAmount())
                .principalPaid(coreResponse.getPrincipalPaid())
                .interestPaid(coreResponse.getInterestPaid())
                .penaltyPaid(coreResponse.getPenaltyPaid())
            .coreTxRef(coreResponse.getTransactionId())
                .paymentMethod(request.getPaymentMethod())
            .result(coreResponse.getStatus() != null ? coreResponse.getStatus() : "SUCCESS")
                .notes(request.getNotes())
            .paidDate(coreResponse.getRepaymentTime() != null ? coreResponse.getRepaymentTime() : LocalDateTime.now())
                .build();
        
        paymentHistoryRepository.save(paymentHistory);
        log.info("[REPAY-008] Payment history recorded: {}", paymentHistory.getPaymentId());
        
        // Get customer info for event
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfo(loanAccount.getCustomerId());
        
        // Publish event
        RepaymentSuccessEvent event = RepaymentSuccessEvent.builder()
            .loanAccountId(loanAccount.getLoanId())
                .cifId(loanAccount.getCustomerId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
            .paymentHistoryId(paymentHistory.getPaymentId())
            .scheduleId(schedule.getScheduleId())
                .installmentNumber(schedule.getInstallmentNo())
                .paidAmount(coreResponse.getPaidAmount())
                .principalPaid(coreResponse.getPrincipalPaid())
                .interestPaid(coreResponse.getInterestPaid())
                .outstandingPrincipal(coreResponse.getOutstandingPrincipal())
                .transactionId(coreResponse.getTransactionId())
                .paymentTime(coreResponse.getRepaymentTime())
                .eventTime(LocalDateTime.now())
                .build();
        
        eventProducer.publishRepaymentSuccessEvent(event);
        log.info("[REPAY-009] Repayment completed successfully");
        
        return RepaymentResponse.builder()
            .loanAccountId(loanAccount.getLoanId())
                .transactionId(coreResponse.getTransactionId())
                .paidAmount(coreResponse.getPaidAmount())
                .principalPaid(coreResponse.getPrincipalPaid())
                .interestPaid(coreResponse.getInterestPaid())
                .penaltyPaid(coreResponse.getPenaltyPaid())
                .outstandingPrincipal(coreResponse.getOutstandingPrincipal())
                .paymentTime(coreResponse.getRepaymentTime())
                .status("SUCCESS")
                .message("Payment processed successfully")
                .build();
    }
    
    @Override
    @Transactional
    public RepaymentResponse earlySettlement(EarlySettlementRequest request) {
        log.info("[SETTLE-001] Processing early settlement for loan: {}", request.getLoanAccountId());
        
        LoanAccount loanAccount = accountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, 
                "Loan ID: " + request.getLoanAccountId()));
        
        if (loanAccount.getStatus() == LoanStatus.CLOSED) {
            log.error("[SETTLE-002] Loan already closed");
            throw new LoanServiceException(ErrorCode.ACC_004, "Loan already closed");
        }
        
        // Calculate total outstanding
        BigDecimal totalOutstanding = scheduleService.calculateEarlySettlementAmount(request.getLoanAccountId());
        
        if (totalOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            log.error("[SETTLE-003] No outstanding amount to settle");
            throw new LoanServiceException(ErrorCode.ACC_005, "Outstanding principal is zero");
        }
        
        // Call Core Banking to close loan
        CoreLoanRepaymentResponse coreResponse;
        try {
            coreResponse = coreBankingClient.closeLoan(
                    loanAccount.getLoanId(), 
                    loanAccount.getRepaymentAccount());
            log.info("[SETTLE-004] Core Banking closure successful. TxId: {}", coreResponse.getTransactionId());
            
        } catch (FeignException e) {
            log.error("[SETTLE-005] Core Banking closure failed", e);
            throw new LoanServiceException(ErrorCode.CORE_004, "Early settlement failed in Core Banking", e);
        }
        
        // Update all remaining installments to CANCELLED
        List<RepaymentSchedule> pendingSchedules = scheduleRepository
            .findByLoanIdAndStatusInOrderByInstallmentNoAsc(
                loanAccount.getLoanId(), 
                        List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE));
        
        pendingSchedules.forEach(schedule -> {
            schedule.setStatus(InstallmentStatus.CANCELLED);
        });
        scheduleRepository.saveAll(pendingSchedules);
        log.info("[SETTLE-006] Cancelled {} remaining installments", pendingSchedules.size());
        
        // Update loan account
        loanAccount.setOutstandingPrincipal(BigDecimal.ZERO);
        loanAccount.setStatus(LoanStatus.CLOSED);
        accountRepository.save(loanAccount);
        
        // Create payment history
        LoanPaymentHistory paymentHistory = LoanPaymentHistory.builder()
            .loanId(loanAccount.getLoanId())
                .paidAmount(coreResponse.getPaidAmount())
                .principalPaid(coreResponse.getPrincipalPaid())
                .interestPaid(coreResponse.getInterestPaid())
                .penaltyPaid(coreResponse.getPenaltyPaid())
            .coreTxRef(coreResponse.getTransactionId())
                .paymentMethod(request.getPaymentMethod())
                .notes("Early settlement")
            .result(coreResponse.getStatus() != null ? coreResponse.getStatus() : "SUCCESS")
            .paidDate(coreResponse.getRepaymentTime() != null ? coreResponse.getRepaymentTime() : LocalDateTime.now())
                .build();
        
        paymentHistoryRepository.save(paymentHistory);
        
        // Get customer info for event
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfo(loanAccount.getCustomerId());
        
        // Publish event
        LoanClosedEvent event = LoanClosedEvent.builder()
            .loanAccountId(loanAccount.getLoanId())
                .cifId(loanAccount.getCustomerId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .totalPaid(coreResponse.getPaidAmount())
                .totalInterestPaid(coreResponse.getInterestPaid())
                .totalPenaltyPaid(coreResponse.getPenaltyPaid())
                .earlySettlement(true)
                .transactionId(coreResponse.getTransactionId())
                .closedTime(coreResponse.getRepaymentTime())
                .eventTime(LocalDateTime.now())
                .build();
        
        eventProducer.publishLoanClosedEvent(event);
        log.info("[SETTLE-007] Early settlement completed successfully");
        
        return RepaymentResponse.builder()
            .loanAccountId(loanAccount.getLoanId())
                .transactionId(coreResponse.getTransactionId())
                .paidAmount(coreResponse.getPaidAmount())
                .principalPaid(coreResponse.getPrincipalPaid())
                .interestPaid(coreResponse.getInterestPaid())
                .penaltyPaid(coreResponse.getPenaltyPaid())
                .outstandingPrincipal(BigDecimal.ZERO)
                .paymentTime(coreResponse.getRepaymentTime())
                .status("SUCCESS")
                .message("Loan settled successfully")
                .build();
    }
    
    @Override
    public List<LoanPaymentHistoryResponse> getPaymentHistory(String loanAccountId) {
        log.info("[HISTORY-001] Getting payment history for loan: {}", loanAccountId);
        
        List<LoanPaymentHistory> history = paymentHistoryRepository
            .findByLoanIdOrderByCreatedAtDesc(loanAccountId);
        
        return history.stream()
                .map(paymentHistoryMapper::toResponse)
                .toList();
    }
}
