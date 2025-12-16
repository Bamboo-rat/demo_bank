package com.example.notificationserrvice.service;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;

/**
 * Interface cho WebSocket notification service
 */
public interface WebSocketNotificationService {
    
    /**
     * Gửi notification qua WebSocket cho người gửi và người nhận
     * 
     * @param event Transaction notification event
     */
    void sendTransactionNotification(TransactionNotificationEvent event);
    
    /**
     * Gửi broadcast notification đến tất cả users
     * 
     * @param message Nội dung thông báo
     */
    void sendBroadcastNotification(String message);
}
