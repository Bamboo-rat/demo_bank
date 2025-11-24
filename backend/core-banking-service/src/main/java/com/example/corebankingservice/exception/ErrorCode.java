package com.example.corebankingservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // CIF related errors
    CIF_NOT_FOUND("CIF_001", "CIF not found", "Không tìm thấy CIF", HttpStatus.NOT_FOUND),
    CIF_ALREADY_EXISTS("CIF_002", "CIF already exists", "CIF đã tồn tại", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("CIF_003", "Username already exists", "Tên người dùng đã tồn tại", HttpStatus.CONFLICT),
    NATIONAL_ID_ALREADY_EXISTS("CIF_004", "National ID already registered", "CMND/CCCD đã được đăng ký", HttpStatus.CONFLICT),
    INVALID_CIF_STATUS_TRANSITION("CIF_005", "Invalid CIF status transition", "Chuyển đổi trạng thái CIF không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CIF_ACTION("CIF_006", "Invalid CIF action", "Hành động CIF không hợp lệ", HttpStatus.BAD_REQUEST),
    CIF_GENERATION_FAILED("CIF_007", "Failed to generate CIF", "Không thể tạo mã CIF", HttpStatus.INTERNAL_SERVER_ERROR),

    // Account related errors
    ACCOUNT_NOT_FOUND("ACC_001", "Account not found", "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE("ACC_002", "Insufficient account balance", "Số dư tài khoản không đủ", HttpStatus.BAD_REQUEST),
    ACCOUNT_BLOCKED("ACC_003", "Account is blocked", "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    ACCOUNT_CLOSED("ACC_004", "Account is closed", "Tài khoản đã bị đóng", HttpStatus.FORBIDDEN),

    // Transaction related errors
    TRANSACTION_NOT_FOUND("TXN_001", "Transaction not found", "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),
    TRANSACTION_FAILED("TXN_002", "Transaction failed", "Giao dịch thất bại", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_AMOUNT("TXN_003", "Invalid transaction amount", "Số tiền giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    DAILY_LIMIT_EXCEEDED("TXN_004", "Daily transaction limit exceeded", "Vượt quá hạn mức giao dịch hàng ngày", HttpStatus.BAD_REQUEST),

    // Validation errors
    INVALID_REQUEST("VAL_001", "Invalid request data", "Dữ liệu yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING("VAL_002", "Required field missing", "Thiếu trường dữ liệu bắt buộc", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("VAL_003", "Invalid data format", "Định dạng dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),

    // System errors
    INTERNAL_SERVER_ERROR("SYS_001", "Internal server error", "Lỗi hệ thống nội bộ", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SYS_002", "Service temporarily unavailable", "Dịch vụ tạm thời không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR("SYS_003", "Database operation failed", "Thao tác cơ sở dữ liệu thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR("SYS_004", "External service error", "Lỗi dịch vụ bên ngoài", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
    private final String vietnameseMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, String vietnameseMessage, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.vietnameseMessage = vietnameseMessage;
        this.httpStatus = httpStatus;
    }
}