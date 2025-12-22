package com.example.notificationserrvice.events.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Loan Event Consumer - Listen to loan-related Kafka events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventConsumer {
    
    private final ObjectMapper objectMapper;
    private final LoanNotificationService loanNotificationService;
    
    @KafkaListener(topics = "loan.approved", groupId = "notification-service-group")
    public void consumeLoanApprovedEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("[LOAN-EVENT-APPROVED] Received loan approved event");
            
            LoanApprovedEvent event = objectMapper.readValue(message, LoanApprovedEvent.class);
            loanNotificationService.handleLoanApproved(event);
            
            acknowledgment.acknowledge();
            log.info("[LOAN-EVENT-APPROVED] Event processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("[LOAN-EVENT-APPROVED] Failed to process event", e);
        }
    }
    
    @KafkaListener(topics = "loan.disbursed", groupId = "notification-service-group")
    public void consumeLoanDisbursedEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("[LOAN-EVENT-DISBURSED] Received loan disbursed event");
            
            LoanDisbursedEvent event = objectMapper.readValue(message, LoanDisbursedEvent.class);
            loanNotificationService.handleLoanDisbursed(event);
            
            acknowledgment.acknowledge();
            log.info("[LOAN-EVENT-DISBURSED] Event processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("[LOAN-EVENT-DISBURSED] Failed to process event", e);
        }
    }
    
    @KafkaListener(topics = "loan.repayment.due", groupId = "notification-service-group")
    public void consumeRepaymentDueEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("[LOAN-EVENT-DUE] Received repayment due event");
            
            RepaymentDueEvent event = objectMapper.readValue(message, RepaymentDueEvent.class);
            loanNotificationService.handleRepaymentDue(event);
            
            acknowledgment.acknowledge();
            log.info("[LOAN-EVENT-DUE] Event processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("[LOAN-EVENT-DUE] Failed to process event", e);
        }
    }
    
    @KafkaListener(topics = "loan.repayment.success", groupId = "notification-service-group")
    public void consumeRepaymentSuccessEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("[LOAN-EVENT-SUCCESS] Received repayment success event");
            
            RepaymentSuccessEvent event = objectMapper.readValue(message, RepaymentSuccessEvent.class);
            loanNotificationService.handleRepaymentSuccess(event);
            
            acknowledgment.acknowledge();
            log.info("[LOAN-EVENT-SUCCESS] Event processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("[LOAN-EVENT-SUCCESS] Failed to process event", e);
        }
    }
    
    @KafkaListener(topics = "loan.overdue", groupId = "notification-service-group")
    public void consumeLoanOverdueEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("[LOAN-EVENT-OVERDUE] Received loan overdue event");
            
            LoanOverdueEvent event = objectMapper.readValue(message, LoanOverdueEvent.class);
            loanNotificationService.handleLoanOverdue(event);
            
            acknowledgment.acknowledge();
            log.info("[LOAN-EVENT-OVERDUE] Event processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("[LOAN-EVENT-OVERDUE] Failed to process event", e);
        }
    }
    
    @KafkaListener(topics = "loan.closed", groupId = "notification-service-group")
    public void consumeLoanClosedEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("[LOAN-EVENT-CLOSED] Received loan closed event");
            
            LoanClosedEvent event = objectMapper.readValue(message, LoanClosedEvent.class);
            loanNotificationService.handleLoanClosed(event);
            
            acknowledgment.acknowledge();
            log.info("[LOAN-EVENT-CLOSED] Event processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("[LOAN-EVENT-CLOSED] Failed to process event", e);
        }
    }
}
