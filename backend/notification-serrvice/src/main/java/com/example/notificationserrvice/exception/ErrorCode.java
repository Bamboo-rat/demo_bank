package com.example.notificationserrvice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    // General errors (6000-6099)
    INTERNAL_SERVER_ERROR("E6000", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("E6001", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("E6002", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("E6003", "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E6004", "Không có quyền truy cập", HttpStatus.FORBIDDEN),
    
    // Notification errors (6100-6199)
    NOTIFICATION_NOT_FOUND("E6100", "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    NOTIFICATION_ALREADY_READ("E6101", "Thông báo đã được đọc", HttpStatus.BAD_REQUEST),
    NOTIFICATION_CREATION_FAILED("E6102", "Tạo thông báo thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_UPDATE_FAILED("E6103", "Cập nhật thông báo thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_ACCESS_DENIED("E6104", "Không có quyền truy cập thông báo này", HttpStatus.FORBIDDEN),
    INVALID_NOTIFICATION_TYPE("E6105", "Loại thông báo không hợp lệ", HttpStatus.BAD_REQUEST),
    
    // Email notification errors (6200-6299)
    EMAIL_SEND_FAILED("E6200", "Gửi email thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_TEMPLATE_NOT_FOUND("E6201", "Không tìm thấy mẫu email", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_INVALID_RECIPIENT("E6202", "Email người nhận không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_SERVICE_UNAVAILABLE("E6203", "Dịch vụ email không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    
    // WebSocket notification errors (6300-6399)
    WEBSOCKET_CONNECTION_FAILED("E6300", "Kết nối WebSocket thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    WEBSOCKET_SEND_FAILED("E6301", "Gửi tin nhắn WebSocket thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    WEBSOCKET_SESSION_NOT_FOUND("E6302", "Không tìm thấy phiên WebSocket", HttpStatus.NOT_FOUND),
    
    // Kafka consumer errors (6400-6499)
    KAFKA_MESSAGE_PROCESSING_FAILED("E6400", "Xử lý tin nhắn Kafka thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    KAFKA_INVALID_MESSAGE_FORMAT("E6401", "Định dạng tin nhắn Kafka không hợp lệ", HttpStatus.BAD_REQUEST),
    KAFKA_CONNECTION_ERROR("E6402", "Lỗi kết nối Kafka", HttpStatus.SERVICE_UNAVAILABLE),
    
    // Database errors (6500-6599)
    DATABASE_ERROR("E6500", "Lỗi cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    CONSTRAINT_VIOLATION("E6501", "Vi phạm ràng buộc dữ liệu", HttpStatus.CONFLICT),
    DATA_INTEGRITY_ERROR("E6502", "Lỗi toàn vẹn dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage(Object... args) {
        if (args == null || args.length == 0) {
            return this.message;
        }
        return String.format(this.message, args);
    }
}
