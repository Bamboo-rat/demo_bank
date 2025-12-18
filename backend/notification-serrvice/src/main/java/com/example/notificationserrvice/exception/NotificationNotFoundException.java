package com.example.notificationserrvice.exception;

import java.util.Map;

/**
 * Exception khi không tìm thấy notification
 */
public class NotificationNotFoundException extends BaseException {
    
    public NotificationNotFoundException(String notificationId, String customerId) {
        super(
            ErrorCode.NOTIFICATION_NOT_FOUND,
            String.format("Không tìm thấy thông báo %s cho khách hàng %s", notificationId, customerId),
            Map.of(
                "notificationId", notificationId,
                "customerId", customerId
            )
        );
    }
    
    public NotificationNotFoundException(String message) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, message);
    }
    
    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
