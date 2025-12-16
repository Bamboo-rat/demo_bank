package com.example.notificationserrvice.service.impl;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.notificationserrvice.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${notification.websocket.enabled:true}")
    private boolean websocketEnabled;

    @Override
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

    @Override
    public void sendBroadcastNotification(String message) {
        if (!websocketEnabled) {
            log.info("WebSocket notification is disabled");
            return;
        }
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "BROADCAST");
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("Broadcast notification sent: {}", message);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}
