package com.example.loanservice.service.impl;

import com.example.loanservice.dto.response.RepaymentScheduleResponse;
import com.example.loanservice.entity.LoanAccount;
import com.example.loanservice.entity.RepaymentSchedule;
import com.example.loanservice.entity.enums.InstallmentStatus;
import com.example.loanservice.entity.enums.RepaymentMethod;
import com.example.loanservice.exception.ErrorCode;
import com.example.loanservice.exception.LoanServiceException;
import com.example.loanservice.mapper.RepaymentScheduleMapper;
import com.example.loanservice.repository.LoanAccountRepository;
import com.example.loanservice.repository.RepaymentScheduleRepository;
import com.example.loanservice.service.RepaymentScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepaymentScheduleServiceImpl implements RepaymentScheduleService {
    
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanAccountRepository accountRepository;
    private final RepaymentScheduleMapper scheduleMapper;
    
    private static final int DAYS_IN_YEAR = 365;
    private static final int MONTHS_IN_YEAR = 12;
    
    @Override
    @Transactional
    public void generateSchedule(String loanAccountId) {
        log.info("[SCH-GEN-001] Generating repayment schedule for loan: {}", loanAccountId);
        
        LoanAccount loanAccount = accountRepository.findById(loanAccountId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, "Loan ID: " + loanAccountId));
        
        // Check if schedule already exists
        List<RepaymentSchedule> existing = scheduleRepository.findByLoanIdOrderByInstallmentNoAsc(loanAccount.getLoanId());
        if (!existing.isEmpty()) {
            log.error("[SCH-GEN-002] Schedule already exists for loan: {}", loanAccountId);
            throw new LoanServiceException(ErrorCode.SCH_002, "Loan ID: " + loanAccountId);
        }
        
        List<RepaymentSchedule> schedules;
        if (loanAccount.getRepaymentMethod() == RepaymentMethod.EQUAL_PRINCIPAL) {
            schedules = generateEqualPrincipalSchedule(loanAccount);
        } else if (loanAccount.getRepaymentMethod() == RepaymentMethod.EQUAL_INSTALLMENT) {
            schedules = generateAnnuitySchedule(loanAccount);
        } else {
            log.error("[SCH-GEN-003] Unsupported repayment method: {}", loanAccount.getRepaymentMethod());
            throw new LoanServiceException(ErrorCode.SCH_003, 
                    "Repayment method: " + loanAccount.getRepaymentMethod());
        }
        
        scheduleRepository.saveAll(schedules);
        log.info("[SCH-GEN-004] Generated {} installments for loan: {}", schedules.size(), loanAccountId);
    }
    
    /**
     * Equal Principal - Principal equal each period, interest decreases
     * Principal per period = Total Principal / Number of periods
     * Interest per period = Outstanding Balance × Monthly Rate
     */
    private List<RepaymentSchedule> generateEqualPrincipalSchedule(LoanAccount loanAccount) {
        log.info("[SCH-CALC-001] Calculating equal principal schedule");
        
        List<RepaymentSchedule> schedules = new ArrayList<>();
        BigDecimal principal = loanAccount.getApprovedAmount();
        BigDecimal monthlyRate = loanAccount.getInterestRateSnapshot()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(MONTHS_IN_YEAR), 10, RoundingMode.HALF_UP);
        
        BigDecimal principalPerPeriod = principal.divide(
                BigDecimal.valueOf(loanAccount.getTenor()), 2, RoundingMode.HALF_UP);
        
        BigDecimal outstandingBalance = principal;
        LocalDate dueDate = loanAccount.getStartDate().plusMonths(1);
        
        for (int i = 1; i <= loanAccount.getTenor(); i++) {
            BigDecimal interestAmount = outstandingBalance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);
            
            // Last period adjustment
            BigDecimal currentPrincipal = (i == loanAccount.getTenor()) 
                    ? outstandingBalance 
                    : principalPerPeriod;
            
            BigDecimal totalAmount = currentPrincipal.add(interestAmount);
            
            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .loanId(loanAccount.getLoanId())
                    .installmentNo(i)
                    .dueDate(dueDate)
                    .principalAmount(currentPrincipal)
                    .interestAmount(interestAmount)
                    .totalAmount(totalAmount)
                    .status(InstallmentStatus.PENDING)
                    .build();
            
            schedules.add(schedule);
            
            outstandingBalance = outstandingBalance.subtract(currentPrincipal);
            dueDate = dueDate.plusMonths(1);
        }
        
        log.info("[SCH-CALC-002] Equal principal schedule calculated: {} periods", schedules.size());
        return schedules;
    }
    
    /**
     * Annuity - Equal total payment each period
     * PMT = P × [r(1+r)^n] / [(1+r)^n - 1]
     * Where: P = principal, r = monthly rate, n = number of periods
     */
    private List<RepaymentSchedule> generateAnnuitySchedule(LoanAccount loanAccount) {
        log.info("[SCH-CALC-003] Calculating annuity schedule");
        
        List<RepaymentSchedule> schedules = new ArrayList<>();
        BigDecimal principal = loanAccount.getApprovedAmount();
        BigDecimal monthlyRate = loanAccount.getInterestRateSnapshot()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(MONTHS_IN_YEAR), 10, RoundingMode.HALF_UP);
        
        int periods = loanAccount.getTenor();
        
        // Calculate PMT using formula
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowerN = onePlusR.pow(periods);
        
        BigDecimal pmt = principal.multiply(monthlyRate)
                .multiply(onePlusRPowerN)
                .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        
        BigDecimal outstandingBalance = principal;
        LocalDate dueDate = loanAccount.getStartDate().plusMonths(1);
        
        for (int i = 1; i <= periods; i++) {
            BigDecimal interestAmount = outstandingBalance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal principalAmount = pmt.subtract(interestAmount);
            
            // Last period adjustment
            if (i == periods) {
                principalAmount = outstandingBalance;
                pmt = principalAmount.add(interestAmount);
            }
            
            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .loanId(loanAccount.getLoanId())
                    .installmentNo(i)
                    .dueDate(dueDate)
                    .principalAmount(principalAmount)
                    .interestAmount(interestAmount)
                    .totalAmount(pmt)
                    .status(InstallmentStatus.PENDING)
                    .build();
            
            schedules.add(schedule);
            
            outstandingBalance = outstandingBalance.subtract(principalAmount);
            dueDate = dueDate.plusMonths(1);
        }
        
        log.info("[SCH-CALC-004] Annuity schedule calculated: {} periods, PMT: {}", schedules.size(), pmt);
        return schedules;
    }
    
    @Override
    public List<RepaymentScheduleResponse> getSchedule(String loanAccountId) {
        log.info("[SCH-GET-001] Getting repayment schedule for loan: {}", loanAccountId);
        
        List<RepaymentSchedule> schedules = scheduleRepository
                .findByLoanIdOrderByInstallmentNoAsc(loanAccountId);
        
        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }
    
    @Override
    public RepaymentScheduleResponse getCurrentInstallment(String loanAccountId) {
        log.info("[SCH-CURRENT-001] Getting current installment for loan: {}", loanAccountId);
        
        RepaymentSchedule schedule = scheduleRepository
                                .findFirstByLoanIdAndStatusOrderByInstallmentNoAsc(loanAccountId, InstallmentStatus.PENDING)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.PAY_003, "Loan ID: " + loanAccountId));
        
        return scheduleMapper.toResponse(schedule);
    }
    
    @Override
    public List<RepaymentScheduleResponse> getOverdueInstallments(String loanAccountId) {
        log.info("[SCH-OVERDUE-001] Getting overdue installments for loan: {}", loanAccountId);
        
        List<RepaymentSchedule> schedules = scheduleRepository
                .findByLoanIdAndStatusOrderByInstallmentNoAsc(loanAccountId, InstallmentStatus.OVERDUE);
        
        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }
    
    @Override
    public BigDecimal calculateEarlySettlementAmount(String loanAccountId) {
        log.info("[SCH-SETTLE-001] Calculating early settlement amount for loan: {}", loanAccountId);
        
        LoanAccount loanAccount = accountRepository.findById(loanAccountId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, "Loan ID: " + loanAccountId));
        
        // Get all pending and overdue installments
        List<RepaymentSchedule> pendingSchedules = scheduleRepository
                .findByLoanIdAndStatusInOrderByInstallmentNoAsc(
                        loanAccountId, List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE));
        
        if (pendingSchedules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Sum all remaining principal
        BigDecimal totalPrincipal = pendingSchedules.stream()
                .map(RepaymentSchedule::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate accrued interest from last payment to today
        BigDecimal accruedInterest = calculateAccruedInterest(loanAccount);
        
        BigDecimal totalAmount = totalPrincipal.add(accruedInterest);
        log.info("[SCH-SETTLE-002] Early settlement amount: {} (Principal: {}, Interest: {})", 
                totalAmount, totalPrincipal, accruedInterest);
        
        return totalAmount;
    }
    
    private BigDecimal calculateAccruedInterest(LoanAccount loanAccount) {
        BigDecimal outstanding = loanAccount.getOutstandingPrincipal();
        BigDecimal dailyRate = loanAccount.getInterestRateSnapshot()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, RoundingMode.HALF_UP);
        
        // Days from last payment to today
        // For simplicity, assuming from disbursement date
        long daysAccrued = java.time.temporal.ChronoUnit.DAYS.between(
                loanAccount.getStartDate(), LocalDate.now());
        
        return outstanding.multiply(dailyRate)
                .multiply(BigDecimal.valueOf(daysAccrued))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
