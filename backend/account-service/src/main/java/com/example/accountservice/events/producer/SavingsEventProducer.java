package com.example.accountservice.events.producer;

import com.example.accountservice.events.model.SavingsMaturedEvent;
import com.example.accountservice.events.model.SavingsOpenedEvent;
import com.example.accountservice.events.model.SavingsWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer cho Savings events
 * Publish events để notification-service consume
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.savings-opened}")
    private String savingsOpenedTopic;

    @Value("${kafka.topic.savings-withdrawn}")
    private String savingsWithdrawnTopic;

    @Value("${kafka.topic.savings-matured}")
    private String savingsMaturedTopic;

    /**
     * Publish event khi mở sổ tiết kiệm
     */
    public void publishSavingsOpenedEvent(SavingsOpenedEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing SavingsOpenedEvent for savingsAccountId: {}", event.getSavingsAccountId());
        
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                savingsOpenedTopic, 
                event.getCustomerId(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA-PRODUCER] Successfully sent SavingsOpenedEvent to topic: {} with offset: {}", 
                             savingsOpenedTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA-PRODUCER] Failed to send SavingsOpenedEvent to topic: {}", 
                             savingsOpenedTopic, ex);
                }
            });
        } catch (Exception e) {
            log.error("[KAFKA-PRODUCER] Error publishing SavingsOpenedEvent", e);
        }
    }

    /**
     * Publish event khi rút tiền trước hạn
     */
    public void publishSavingsWithdrawnEvent(SavingsWithdrawnEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing SavingsWithdrawnEvent for savingsAccountId: {}", event.getSavingsAccountId());
        
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                savingsWithdrawnTopic, 
                event.getCustomerId(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA-PRODUCER] Successfully sent SavingsWithdrawnEvent to topic: {} with offset: {}", 
                             savingsWithdrawnTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA-PRODUCER] Failed to send SavingsWithdrawnEvent to topic: {}", 
                             savingsWithdrawnTopic, ex);
                }
            });
        } catch (Exception e) {
            log.error("[KAFKA-PRODUCER] Error publishing SavingsWithdrawnEvent", e);
        }
    }

    /**
     * Publish event khi sổ tiết kiệm đáo hạn
     */
    public void publishSavingsMaturedEvent(SavingsMaturedEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing SavingsMaturedEvent for savingsAccountId: {}", event.getSavingsAccountId());
        
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                savingsMaturedTopic, 
                event.getCustomerId(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA-PRODUCER] Successfully sent SavingsMaturedEvent to topic: {} with offset: {}", 
                             savingsMaturedTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA-PRODUCER] Failed to send SavingsMaturedEvent to topic: {}", 
                             savingsMaturedTopic, ex);
                }
            });
        } catch (Exception e) {
            log.error("[KAFKA-PRODUCER] Error publishing SavingsMaturedEvent", e);
        }
    }
}
