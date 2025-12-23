package com.example.loanservice.scheduler;

import com.example.loanservice.client.CustomerServiceClient;
import com.example.loanservice.dto.customer.CustomerInfoResponse;
import com.example.loanservice.entity.LoanAccount;
import com.example.loanservice.entity.RepaymentSchedule;
import com.example.loanservice.entity.enums.InstallmentStatus;
import com.example.loanservice.entity.enums.LoanStatus;
import com.example.loanservice.event.LoanEventProducer;
import com.example.loanservice.event.LoanOverdueEvent;
import com.example.loanservice.event.RepaymentDueEvent;
import com.example.loanservice.repository.LoanAccountRepository;
import com.example.loanservice.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Overdue Monitoring Scheduler
 * - Check for upcoming due dates
 * - Mark overdue installments
 * - Publish notification events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueMonitoringScheduler {
    
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanAccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;
    private final LoanEventProducer eventProducer;
    
    /**
     * Check for upcoming due dates and send reminders
     * Run daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkUpcomingDueDates() {
        log.info("[SCHEDULER-DUE-001] Starting upcoming due date check");
        
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        
        // Find installments due in next 3 days
        List<RepaymentSchedule> upcomingSchedules = scheduleRepository
                .findByStatusAndDueDateBetween(InstallmentStatus.PENDING, today, threeDaysLater);
        
        log.info("[SCHEDULER-DUE-002] Found {} upcoming installments", upcomingSchedules.size());
        
        for (RepaymentSchedule schedule : upcomingSchedules) {
            try {
                sendDueReminder(schedule);
            } catch (Exception e) {
                log.error("[SCHEDULER-DUE-003] Failed to send reminder for schedule: {}", 
                        schedule.getScheduleId(), e);
            }
        }
        
        log.info("[SCHEDULER-DUE-004] Upcoming due date check completed");
    }
    
    /**
     * Check for overdue installments
     * Run daily at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkOverdueInstallments() {
        log.info("[SCHEDULER-OVERDUE-001] Starting overdue installment check");
        
        LocalDate today = LocalDate.now();
        
        // Find installments that are past due and still pending
        List<RepaymentSchedule> overdueSchedules = scheduleRepository
                .findByStatusAndDueDateBefore(InstallmentStatus.PENDING, today);
        
        log.info("[SCHEDULER-OVERDUE-002] Found {} overdue installments", overdueSchedules.size());
        
        for (RepaymentSchedule schedule : overdueSchedules) {
            try {
                markAsOverdue(schedule);
            } catch (Exception e) {
                log.error("[SCHEDULER-OVERDUE-003] Failed to process overdue schedule: {}", 
                        schedule.getScheduleId(), e);
            }
        }
        
        log.info("[SCHEDULER-OVERDUE-004] Overdue installment check completed");
    }
    
    /**
     * Check for severely overdue loans (30+ days)
     * Run daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkSeverelyOverdueLoans() {
        log.info("[SCHEDULER-DEFAULT-001] Starting severely overdue loan check");
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        
        // Find loans with overdue installments older than 30 days
        List<RepaymentSchedule> severelyOverdue = scheduleRepository
                .findByStatusAndDueDateBefore(InstallmentStatus.OVERDUE, thirtyDaysAgo);
        
        log.info("[SCHEDULER-DEFAULT-002] Found {} severely overdue installments", severelyOverdue.size());
        
        for (RepaymentSchedule schedule : severelyOverdue) {
            try {
                LoanAccount loanAccount = accountRepository.findById(schedule.getLoanId())
                        .orElse(null);
                
                if (loanAccount != null && loanAccount.getStatus() != LoanStatus.DEFAULT) {
                    loanAccount.setStatus(LoanStatus.DEFAULT);
                    accountRepository.save(loanAccount);
                    log.info("[SCHEDULER-DEFAULT-003] Loan {} marked as DEFAULTED", 
                            loanAccount.getLoanId());
                }
            } catch (Exception e) {
                log.error("[SCHEDULER-DEFAULT-004] Failed to mark loan as defaulted: {}", 
                        schedule.getLoanId(), e);
            }
        }
        
        log.info("[SCHEDULER-DEFAULT-005] Severely overdue loan check completed");
    }
    
    private void sendDueReminder(RepaymentSchedule schedule) {
        LoanAccount loanAccount = accountRepository.findById(schedule.getLoanId())
                .orElseThrow();
        
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfoByCustomerId(loanAccount.getCustomerId());
        
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), schedule.getDueDate());
        
        RepaymentDueEvent event = RepaymentDueEvent.builder()
            .loanAccountId(loanAccount.getLoanId())
                .cifId(customer.getCifId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
            .scheduleId(schedule.getScheduleId())
            .installmentNumber(schedule.getInstallmentNo())
                .dueDate(schedule.getDueDate())
                .dueAmount(schedule.getTotalAmount())
                .principalAmount(schedule.getPrincipalAmount())
                .interestAmount(schedule.getInterestAmount())
                .daysUntilDue((int) daysUntilDue)
                .eventTime(LocalDateTime.now())
                .build();
        
        eventProducer.publishRepaymentDueEvent(event);
        log.info("[SCHEDULER-DUE-REMIND] Sent reminder for loan: {}, installment: {}", 
            loanAccount.getLoanId(), schedule.getInstallmentNo());
    }
    
    private void markAsOverdue(RepaymentSchedule schedule) {
        // Update installment status to OVERDUE
        schedule.setStatus(InstallmentStatus.OVERDUE);
        scheduleRepository.save(schedule);
        
        // Update loan account status to OVERDUE
        LoanAccount loanAccount = accountRepository.findById(schedule.getLoanId())
                .orElseThrow();
        
        if (loanAccount.getStatus() == LoanStatus.ACTIVE) {
            loanAccount.setStatus(LoanStatus.OVERDUE);
            accountRepository.save(loanAccount);
        }
        
        // Calculate days overdue
        long daysOverdue = ChronoUnit.DAYS.between(schedule.getDueDate(), LocalDate.now());
        
        // Get customer info
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfoByCustomerId(loanAccount.getCustomerId());
        
        // Publish overdue event
        LoanOverdueEvent event = LoanOverdueEvent.builder()
            .loanAccountId(loanAccount.getLoanId())
                .cifId(customer.getCifId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
            .scheduleId(schedule.getScheduleId())
            .installmentNumber(schedule.getInstallmentNo())
                .dueDate(schedule.getDueDate())
                .daysOverdue((int) daysOverdue)
                .overdueAmount(schedule.getTotalAmount())
                .principalOverdue(schedule.getPrincipalAmount())
                .interestOverdue(schedule.getInterestAmount())
                .penaltyAmount(schedule.getPenaltyAmount())
                .totalDue(schedule.getTotalAmount())
                .eventTime(LocalDateTime.now())
                .build();
        
        eventProducer.publishLoanOverdueEvent(event);
        
        log.info("[SCHEDULER-OVERDUE-MARK] Marked as overdue - Loan: {}, Installment: {}, Days: {}", 
            loanAccount.getLoanId(), schedule.getInstallmentNo(), daysOverdue);
    }
}
