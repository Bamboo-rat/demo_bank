package com.example.notificationserrvice.service;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;

/**
 * Interface cho email notification service
 */
public interface EmailNotificationService {
    
    /**
     * Gửi email thông báo khi chuyển tiền thành công
     * 
     * @param event Transaction notification event
     */
    void sendTransactionSuccessEmail(TransactionNotificationEvent event);
    
    /**
     * Gửi email đơn giản
     * 
     * @param toEmail Email người nhận
     * @param toName Tên người nhận
     * @param subject Tiêu đề email
     * @param htmlContent Nội dung HTML
     */
    void sendSimpleEmail(String toEmail, String toName, String subject, String htmlContent);
    
    /**
     * Gửi email với nội dung text thuần
     * 
     * @param toEmail Email người nhận
     * @param subject Tiêu đề email
     * @param textContent Nội dung text
     */
    void sendTextEmail(String toEmail, String subject, String textContent);
}
