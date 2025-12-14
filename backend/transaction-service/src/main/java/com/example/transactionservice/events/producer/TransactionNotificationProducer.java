package com.example.transactionservice.events.producer;

import com.example.transactionservice.events.TransactionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service gửi transaction notification events qua Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionNotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.transaction-notification:transaction-notifications}")
    private String topicName;

    /**
     * Gửi notification event khi có giao dịch thành công
     * 
     * @param event Transaction notification event
     */
    public void sendTransactionNotification(TransactionNotificationEvent event) {
        try {
            log.info("Sending transaction notification to Kafka - Transaction ID: {}, Reference: {}", 
                    event.getTransactionId(), event.getTransactionReference());
            
            // Sử dụng transactionReference làm key để đảm bảo các event của cùng 1 transaction 
            // luôn được gửi đến cùng 1 partition (ordering)
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(topicName, event.getTransactionReference(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Transaction notification sent successfully - Topic: {}, Partition: {}, Offset: {}, Transaction: {}", 
                            topicName, 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.getTransactionReference());
                } else {
                    log.error("Failed to send transaction notification - Transaction: {}, Error: {}", 
                            event.getTransactionReference(), ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            // Log error nhưng không throw exception để không ảnh hưởng đến transaction chính
            log.error("Error sending transaction notification to Kafka - Transaction: {}", 
                    event.getTransactionReference(), e);
        }
    }

    /**
     * Gửi notification đồng bộ (chờ kết quả)
     * Sử dụng khi cần đảm bảo event đã được gửi thành công
     */
    public void sendTransactionNotificationSync(TransactionNotificationEvent event) throws Exception {
        log.info("Sending transaction notification to Kafka (sync) - Transaction ID: {}", 
                event.getTransactionId());
        
        SendResult<String, Object> result = kafkaTemplate
                .send(topicName, event.getTransactionReference(), event)
                .get(); // Block và chờ kết quả
        
        log.info("Transaction notification sent successfully (sync) - Partition: {}, Offset: {}", 
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }
}
