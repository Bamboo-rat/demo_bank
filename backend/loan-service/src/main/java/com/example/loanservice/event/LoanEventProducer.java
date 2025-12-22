package com.example.loanservice.event;

import com.example.loanservice.exception.ErrorCode;
import com.example.loanservice.exception.LoanServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC_LOAN_APPROVED = "loan.approved";
    private static final String TOPIC_LOAN_DISBURSED = "loan.disbursed";
    private static final String TOPIC_REPAYMENT_DUE = "loan.repayment.due";
    private static final String TOPIC_REPAYMENT_SUCCESS = "loan.repayment.success";
    private static final String TOPIC_LOAN_OVERDUE = "loan.overdue";
    private static final String TOPIC_LOAN_CLOSED = "loan.closed";
    
    public void publishLoanApprovedEvent(LoanApprovedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendEvent(TOPIC_LOAN_APPROVED, event.getLoanAccountId(), event);
        log.info("[EVENT-LOAN-APPROVED] Published event for loan: {}", event.getLoanAccountId());
    }
    
    public void publishLoanDisbursedEvent(LoanDisbursedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendEvent(TOPIC_LOAN_DISBURSED, event.getLoanAccountId(), event);
        log.info("[EVENT-LOAN-DISBURSED] Published event for loan: {}", event.getLoanAccountId());
    }
    
    public void publishRepaymentDueEvent(RepaymentDueEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendEvent(TOPIC_REPAYMENT_DUE, event.getLoanAccountId(), event);
        log.info("[EVENT-REPAYMENT-DUE] Published event for loan: {}, installment: {}", 
                event.getLoanAccountId(), event.getInstallmentNumber());
    }
    
    public void publishRepaymentSuccessEvent(RepaymentSuccessEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendEvent(TOPIC_REPAYMENT_SUCCESS, event.getLoanAccountId(), event);
        log.info("[EVENT-REPAYMENT-SUCCESS] Published event for loan: {}, installment: {}", 
                event.getLoanAccountId(), event.getInstallmentNumber());
    }
    
    public void publishLoanOverdueEvent(LoanOverdueEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendEvent(TOPIC_LOAN_OVERDUE, event.getLoanAccountId(), event);
        log.info("[EVENT-LOAN-OVERDUE] Published event for loan: {}, days: {}", 
                event.getLoanAccountId(), event.getDaysOverdue());
    }
    
    public void publishLoanClosedEvent(LoanClosedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendEvent(TOPIC_LOAN_CLOSED, event.getLoanAccountId(), event);
        log.info("[EVENT-LOAN-CLOSED] Published event for loan: {}", event.getLoanAccountId());
    }
    
    private void sendEvent(String topic, String key, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[EVENT-PUBLISH-FAILED] Failed to publish event to topic: {}, key: {}", 
                            topic, key, ex);
                } else {
                    log.debug("[EVENT-PUBLISH-SUCCESS] Event published to topic: {}, partition: {}, offset: {}", 
                            topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
            
        } catch (Exception e) {
            log.error("[EVENT-SERIALIZE-ERROR] Failed to serialize event for topic: {}", topic, e);
            throw new LoanServiceException(ErrorCode.NOTIF_001, "Failed to send notification event", e);
        }
    }
}
