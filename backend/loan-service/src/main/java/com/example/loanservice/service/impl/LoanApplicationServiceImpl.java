package com.example.loanservice.service.impl;

import com.example.loanservice.client.CoreBankingClient;
import com.example.loanservice.client.CustomerServiceClient;
import com.example.loanservice.dto.corebanking.CoreLoanDisbursementRequest;
import com.example.loanservice.dto.corebanking.CoreLoanDisbursementResponse;
import com.example.loanservice.dto.customer.CustomerInfoResponse;
import com.example.loanservice.dto.request.LoanApplicationRequest;
import com.example.loanservice.dto.request.LoanApprovalRequest;
import com.example.loanservice.dto.response.LoanApplicationResponse;
import com.example.loanservice.entity.LoanAccount;
import com.example.loanservice.entity.LoanApplication;
import com.example.loanservice.entity.enums.ApplicationStatus;
import com.example.loanservice.entity.enums.LoanStatus;
import com.example.loanservice.event.LoanApprovedEvent;
import com.example.loanservice.event.LoanEventProducer;
import com.example.loanservice.exception.ErrorCode;
import com.example.loanservice.exception.LoanServiceException;
import com.example.loanservice.mapper.LoanApplicationMapper;
import com.example.loanservice.repository.LoanAccountRepository;
import com.example.loanservice.repository.LoanApplicationRepository;
import com.example.loanservice.service.LoanApplicationService;
import com.example.loanservice.service.RepaymentScheduleService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationServiceImpl implements LoanApplicationService {
    
    private final LoanApplicationRepository applicationRepository;
    private final LoanAccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;
    private final CoreBankingClient coreBankingClient;
    private final RepaymentScheduleService scheduleService;
    private final LoanEventProducer eventProducer;
    private final LoanApplicationMapper applicationMapper;
    
    private static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("10000000");
    private static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000");
    private static final int MIN_TERM_MONTHS = 6;
    private static final int MAX_TERM_MONTHS = 240;
    
    @Override
    @Transactional
    public LoanApplicationResponse registerApplication(LoanApplicationRequest request, String customerId) {
        log.info("[APP-REGISTER-001] Registering loan application for customer: {}", customerId);
        
        // Validate loan amount
        if (request.getRequestedAmount().compareTo(MIN_LOAN_AMOUNT) < 0 || 
            request.getRequestedAmount().compareTo(MAX_LOAN_AMOUNT) > 0) {
            log.error("[APP-REGISTER-002] Invalid loan amount: {}", request.getRequestedAmount());
            throw new LoanServiceException(ErrorCode.APP_004, 
                    String.format("Loan amount must be between %s and %s VND", MIN_LOAN_AMOUNT, MAX_LOAN_AMOUNT));
        }
        
        // Validate term
        if (request.getTenor() < MIN_TERM_MONTHS || request.getTenor() > MAX_TERM_MONTHS) {
            log.error("[APP-REGISTER-003] Invalid loan term: {}", request.getTenor());
            throw new LoanServiceException(ErrorCode.APP_005, 
                    String.format("Loan term must be between %d and %d months", MIN_TERM_MONTHS, MAX_TERM_MONTHS));
        }
        
        // Verify customer via Dubbo
        CustomerInfoResponse customer;
        try {
            customer = customerServiceClient.getCustomerInfo(customerId);
            if (customer == null || !"ACTIVE".equals(customer.getStatus()) || !customer.isKycCompleted()) {
                log.error("[APP-REGISTER-004] Customer not eligible: {}", customerId);
                throw new LoanServiceException(ErrorCode.APP_002, "Customer not found or not eligible for loan");
            }
        } catch (Exception e) {
            log.error("[APP-REGISTER-005] Failed to verify customer: {}", customerId, e);
            throw new LoanServiceException(ErrorCode.CUST_001, "Customer service unavailable", e);
        }
        
        // Check for existing pending application
        List<LoanApplication> pendingApps = applicationRepository
                .findByCustomerIdAndStatus(customerId, ApplicationStatus.PENDING_APPROVAL);
        if (!pendingApps.isEmpty()) {
            log.error("[APP-REGISTER-006] Customer has existing pending application: {}", customerId);
            throw new LoanServiceException(ErrorCode.APP_003, "Customer already has pending loan application");
        }
        
        // Create application
        LoanApplication application = LoanApplication.builder()
                .customerId(customerId)
                .requestedAmount(request.getRequestedAmount())
                .tenor(request.getTenor())
                .purpose(request.getPurpose())
                .repaymentMethod(request.getRepaymentMethod())
                .monthlyIncome(request.getMonthlyIncome())
                .employmentStatus(request.getEmploymentStatus())
                .collateralInfo(request.getCollateralInfo())
                .status(ApplicationStatus.PENDING_APPROVAL)
                .notes(request.getNotes())
                .build();
        
        application = applicationRepository.save(application);
        log.info("[APP-REGISTER-007] Loan application created: {}", application.getApplicationId());
        
        return applicationMapper.toResponse(application);
    }
    
    @Override
    @Transactional
    public LoanApplicationResponse approveLoan(String applicationId, LoanApprovalRequest request) {
        log.info("[APP-APPROVE-001] Approving loan application: {}", applicationId);
        
        // Find application
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.APP_001, "Application ID: " + applicationId));
        
        // Validate status
        if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
            log.error("[APP-APPROVE-002] Application not in pending status: {}", applicationId);
            throw new LoanServiceException(ErrorCode.APP_008, 
                    "Application status: " + application.getStatus());
        }
        
        // Get customer info for event
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfo(application.getCustomerId());
        
        // Generate loan number from Core Banking
        String loanNumber;
        try {
            loanNumber = coreBankingClient.generateLoanNumber();
            log.info("[APP-APPROVE-003] Generated loan number: {}", loanNumber);
        } catch (FeignException e) {
            log.error("[APP-APPROVE-004] Failed to generate loan number", e);
            throw new LoanServiceException(ErrorCode.CORE_001, "Core Banking service unavailable", e);
        }
        
        // Create loan account
        String approvedBy = (request.getApprovedBy() == null || request.getApprovedBy().isBlank()) ? "SYSTEM" : request.getApprovedBy();
        LocalDate startDate = LocalDate.now();
        Integer tenor = application.getTenor();

        LoanAccount loanAccount = LoanAccount.builder()
                .loanNumber(loanNumber)
                .applicationId(application.getApplicationId())
                .customerId(application.getCustomerId())
                .approvedAmount(request.getApprovedAmount())
                .outstandingPrincipal(BigDecimal.ZERO)
            .interestRateSnapshot(request.getInterestRate())
            .penaltyRate(request.getPenaltyRate())
            .tenor(tenor)
            .purpose(application.getPurpose())
                .repaymentMethod(application.getRepaymentMethod())
            .startDate(startDate)
            .maturityDate(startDate.plusMonths(tenor))
                .status(LoanStatus.APPROVED)
            .disbursementAccount(request.getDisbursementAccount())
            .repaymentAccount(request.getRepaymentAccount())
            .approvedBy(approvedBy)
            .approvedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();
        
        loanAccount = accountRepository.save(loanAccount);
        log.info("[APP-APPROVE-005] Loan account created: {}", loanAccount.getLoanId());
        
        // Create loan account in Core Banking
        try {
            CoreLoanDisbursementRequest coreRequest = CoreLoanDisbursementRequest.builder()
                    .loanServiceRef(loanAccount.getLoanId())
                    .cifId(application.getCustomerId())
                    .accountId(request.getDisbursementAccount())
                    .disbursementAmount(request.getApprovedAmount())
                    .interestRate(request.getInterestRate())
                    .termMonths(tenor)
                    .disbursementDate(startDate)
                    .maturityDate(loanAccount.getMaturityDate())
                    .notes("Loan approval: " + applicationId)
                    .build();
            
            CoreLoanDisbursementResponse coreResponse = coreBankingClient.createLoanAccount(coreRequest);
            loanAccount.setCoreLoanId(coreResponse.getLoanId());
            accountRepository.save(loanAccount);
            
            log.info("[APP-APPROVE-006] Core loan account created: {}", coreResponse.getLoanId());
            
        } catch (FeignException e) {
            log.error("[APP-APPROVE-007] Failed to create Core loan account", e);
            throw new LoanServiceException(ErrorCode.CORE_002, "Failed to create loan in Core Banking", e);
        }
        
        // Generate repayment schedule
        scheduleService.generateSchedule(loanAccount.getLoanId());
        log.info("[APP-APPROVE-008] Repayment schedule generated for loan: {}", loanAccount.getLoanId());
        
        // Update application status
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedBy(approvedBy);
        application.setReviewedAt(LocalDateTime.now());
        application.setLoanId(loanAccount.getLoanId());
        applicationRepository.save(application);
        
        // Publish event
        LoanApprovedEvent event = LoanApprovedEvent.builder()
            .loanApplicationId(application.getApplicationId())
            .loanAccountId(loanAccount.getLoanId())
                .cifId(application.getCustomerId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .approvedAmount(request.getApprovedAmount())
                .interestRate(request.getInterestRate())
            .termMonths(tenor)
            .expectedDisbursementDate(startDate)
                .eventTime(LocalDateTime.now())
                .build();
        
        eventProducer.publishLoanApprovedEvent(event);
        log.info("[APP-APPROVE-009] Loan approval completed: {}", applicationId);
        
        return applicationMapper.toResponse(application);
    }
    
    @Override
    @Transactional
    public LoanApplicationResponse rejectLoan(String applicationId, String rejectionReason) {
        log.info("[APP-REJECT-001] Rejecting loan application: {}", applicationId);
        
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.APP_001, "Application ID: " + applicationId));
        
        if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
            log.error("[APP-REJECT-002] Application not in pending status: {}", applicationId);
            throw new LoanServiceException(ErrorCode.APP_008, "Application status: " + application.getStatus());
        }
        
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(rejectionReason);
        application.setReviewedAt(LocalDateTime.now());
        application = applicationRepository.save(application);
        
        log.info("[APP-REJECT-003] Loan application rejected: {}", applicationId);
        return applicationMapper.toResponse(application);
    }
    
    @Override
    public LoanApplicationResponse getApplication(String applicationId) {
        log.info("[APP-GET-001] Getting loan application: {}", applicationId);
        
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.APP_001, "Application ID: " + applicationId));
        
        return applicationMapper.toResponse(application);
    }
    
    @Override
    public List<LoanApplicationResponse> getApplicationsByCustomer(String cifId) {
        log.info("[APP-LIST-001] Getting loan applications for customer: {}", cifId);
        
        List<LoanApplication> applications = applicationRepository.findByCustomerId(cifId);
        return applications.stream()
                .map(applicationMapper::toResponse)
                .toList();
    }
    
    @Override
    @Transactional
    public LoanApplicationResponse cancelApplication(String applicationId, String reason) {
        log.info("[APP-CANCEL-001] Cancelling loan application: {}", applicationId);
        
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.APP_001, "Application ID: " + applicationId));
        
        if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
            log.error("[APP-CANCEL-002] Application not in pending status: {}", applicationId);
            throw new LoanServiceException(ErrorCode.APP_008, "Application status: " + application.getStatus());
        }
        
        application.setStatus(ApplicationStatus.CANCELLED);
        application.setNotes(reason);
        application = applicationRepository.save(application);
        
        log.info("[APP-CANCEL-003] Loan application cancelled: {}", applicationId);
        return applicationMapper.toResponse(application);
    }
}
