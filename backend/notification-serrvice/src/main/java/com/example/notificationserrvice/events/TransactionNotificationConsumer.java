package com.example.notificationserrvice.events;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.notificationserrvice.service.EmailNotificationService;
import com.example.notificationserrvice.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionNotificationConsumer {

    private final EmailNotificationService emailNotificationService;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * Lắng nghe transaction notification events từ Kafka
     * Topic: transaction-notifications
     * 
     * @param event Transaction notification event
     * @param partition Kafka partition
     * @param offset Message offset
     * @param acknowledgment Manual acknowledgment
     */
    @KafkaListener(
        topics = "${kafka.topic.transaction-notification:transaction-notifications}",
        groupId = "${spring.kafka.consumer.group-id:notification-service-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionNotification(
            @Payload TransactionNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received transaction notification from Kafka - Transaction: {}, Partition: {}, Offset: {}", 
                    event.getTransactionReference(), partition, offset);
            
            // Validate event
            if (event == null || event.getTransactionId() == null) {
                log.warn("Received invalid transaction notification event");
                acknowledgment.acknowledge();
                return;
            }

            // Process notification
            processNotification(event);

            // Acknowledge message after successful processing
            acknowledgment.acknowledge();
            log.info("Transaction notification processed successfully - Transaction: {}", 
                    event.getTransactionReference());

        } catch (Exception e) {
            log.error("Error processing transaction notification - Transaction: {}, Error: {}", 
                    event.getTransactionReference(), e.getMessage(), e);
            
            // Không acknowledge để Kafka retry (hoặc có thể gửi vào DLQ)
            // Tùy vào retry policy và error handling strategy
            // Ở đây ta acknowledge để tránh block consumer
            acknowledgment.acknowledge();
        }
    }

    /**
     * Xử lý notification: gửi email và WebSocket
     */
    private void processNotification(TransactionNotificationEvent event) {
        log.info("Processing notification for transaction: {}", event.getTransactionReference());

        // 1. Gửi WebSocket notification (real-time)
        try {
            webSocketNotificationService.sendTransactionNotification(event);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
            // Continue with email notification even if WebSocket fails
        }

        // 2. Gửi Email notification
        try {
            emailNotificationService.sendTransactionSuccessEmail(event);
        } catch (Exception e) {
            log.error("Failed to send Email notification", e);
        }

        log.info("Notification processing completed for transaction: {}", event.getTransactionReference());
    }

    /**
     * Error handler cho Kafka listener
     */
    @KafkaHandler(isDefault = true)
    public void handleUnknownMessage(Object message) {
        log.warn("Received unknown message type: {}", message.getClass().getName());
    }
}
