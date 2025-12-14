package com.example.notificationserrvice.service;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Service gửi WebSocket notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${notification.websocket.enabled:true}")
    private boolean websocketEnabled;

    /**
     * Gửi notification qua WebSocket cho người gửi và người nhận
     */
    public void sendTransactionNotification(TransactionNotificationEvent event) {
        if (!websocketEnabled) {
            log.info("WebSocket notification is disabled");
            return;
        }

        try {
            // Gửi thông báo cho người gửi
            if (event.getSenderCustomerId() != null) {
                sendSenderNotification(event);
            }

            // Gửi thông báo cho người nhận
            if (event.getReceiverCustomerId() != null) {
                sendReceiverNotification(event);
            }

        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for transaction: {}", 
                    event.getTransactionReference(), e);
        }
    }

    /**
     * Gửi notification cho người gửi tiền
     * Topic: /user/{customerId}/queue/notifications
     */
    private void sendSenderNotification(TransactionNotificationEvent event) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRANSACTION_SENT");
        notification.put("transactionId", event.getTransactionId());
        notification.put("transactionReference", event.getTransactionReference());
        notification.put("title", "Chuyển tiền thành công");
        notification.put("message", String.format(
                "Bạn đã chuyển %s %s đến %s (%s)",
                formatCurrency(event.getAmount()),
                event.getCurrency(),
                event.getReceiverName(),
                event.getReceiverAccountNumber()
        ));
        notification.put("amount", event.getAmount());
        notification.put("currency", event.getCurrency());
        notification.put("receiverName", event.getReceiverName());
        notification.put("receiverAccount", event.getReceiverAccountNumber());
        notification.put("balanceAfter", event.getSenderBalanceAfter());
        notification.put("timestamp", event.getTransactionTime());
        notification.put("status", "SUCCESS");

        // Gửi đến user-specific queue
        String destination = "/user/" + event.getSenderCustomerId() + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sender WebSocket notification sent to: {} for transaction: {}", 
                event.getSenderCustomerId(), event.getTransactionReference());
    }

    /**
     * Gửi notification cho người nhận tiền
     * Topic: /user/{customerId}/queue/notifications
     */
    private void sendReceiverNotification(TransactionNotificationEvent event) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRANSACTION_RECEIVED");
        notification.put("transactionId", event.getTransactionId());
        notification.put("transactionReference", event.getTransactionReference());
        notification.put("title", "Nhận tiền thành công");
        notification.put("message", String.format(
                "Bạn nhận được %s %s từ %s (%s)",
                formatCurrency(event.getAmount()),
                event.getCurrency(),
                event.getSenderName(),
                event.getSenderAccountNumber()
        ));
        notification.put("amount", event.getAmount());
        notification.put("currency", event.getCurrency());
        notification.put("senderName", event.getSenderName());
        notification.put("senderAccount", event.getSenderAccountNumber());
        notification.put("balanceAfter", event.getReceiverBalanceAfter());
        notification.put("timestamp", event.getTransactionTime());
        notification.put("status", "SUCCESS");

        // Gửi đến user-specific queue
        String destination = "/user/" + event.getReceiverCustomerId() + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Receiver WebSocket notification sent to: {} for transaction: {}", 
                event.getReceiverCustomerId(), event.getTransactionReference());
    }

    /**
     * Gửi broadcast notification đến tất cả users (optional)
     */
    public void sendBroadcastNotification(String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "BROADCAST");
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("Broadcast notification sent: {}", message);
    }

    /**
     * Format currency
     */
    private String formatCurrency(java.math.BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}
