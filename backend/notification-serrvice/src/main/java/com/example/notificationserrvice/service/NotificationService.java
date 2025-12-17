package com.example.notificationserrvice.service;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.notificationserrvice.dto.response.NotificationPageResponse;
import com.example.notificationserrvice.dto.response.NotificationResponse;
import com.example.notificationserrvice.entity.Notification;
import com.example.notificationserrvice.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    /**
     * Lưu notification từ transaction event vào database
     * Sẽ tạo notification cho cả người gửi và người nhận (nếu là internal transfer)
     * 
     * @param event Transaction notification event
     * @return List of saved notifications
     */
    List<Notification> saveTransactionNotifications(TransactionNotificationEvent event);

    /**
     * Lưu một notification vào database
     * 
     * @param notification Notification entity to save
     * @return Saved notification
     */
    Notification saveNotification(Notification notification);

    /**
     * Lấy danh sách thông báo của customer với pagination
     * Sắp xếp theo thời gian gần nhất
     * 
     * @param customerId Customer ID
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated notification response
     */
    NotificationPageResponse getCustomerNotifications(Long customerId, int page, int size);

    /**
     * Lấy danh sách thông báo theo loại
     * 
     * @param customerId Customer ID
     * @param type Notification type
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated notification response
     */
    NotificationPageResponse getCustomerNotificationsByType(Long customerId, NotificationType type, int page, int size);

    /**
     * Lấy chi tiết một thông báo
     * 
     * @param customerId Customer ID
     * @param notificationId Notification ID
     * @return Notification detail
     */
    NotificationResponse getNotificationDetail(Long customerId, String notificationId);

    /**
     * Đánh dấu thông báo đã đọc
     * 
     * @param customerId Customer ID
     * @param notificationId Notification ID
     * @return Updated notification
     */
    NotificationResponse markAsRead(Long customerId, String notificationId);

    /**
     * Đánh dấu tất cả thông báo đã đọc
     * 
     * @param customerId Customer ID
     * @return Số lượng thông báo được đánh dấu
     */
    int markAllAsRead(Long customerId);

    /**
     * Đếm số thông báo chưa đọc
     * Không query full list để tối ưu performance
     * 
     * @param customerId Customer ID
     * @return Số lượng thông báo chưa đọc
     */
    long countUnreadNotifications(Long customerId);
}
