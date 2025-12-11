package com.example.transactionservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Transaction related errors
    TRANSACTION_NOT_FOUND("TRX_001", "Transaction not found", "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_AMOUNT("TRX_002", "Invalid transaction amount", "Số tiền giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("TRX_003", "Insufficient balance", "Số dư không đủ", HttpStatus.BAD_REQUEST),
    TRANSACTION_LIMIT_EXCEEDED("TRX_004", "Transaction limit exceeded", "Vượt quá giới hạn giao dịch", HttpStatus.BAD_REQUEST),
    DAILY_LIMIT_EXCEEDED("TRX_005", "Daily transaction limit exceeded", "Vượt quá giới hạn giao dịch trong ngày", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_TYPE("TRX_006", "Invalid transaction type", "Loại giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    TRANSACTION_ALREADY_PROCESSED("TRX_007", "Transaction already processed", "Giao dịch đã được xử lý", HttpStatus.CONFLICT),
    TRANSACTION_FAILED("TRX_008", "Transaction processing failed", "Xử lý giao dịch thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TRANSACTION_STATUS("TRX_009", "Invalid transaction status", "Trạng thái giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    TRANSACTION_CANNOT_BE_REVERSED("TRX_010", "Transaction cannot be reversed", "Giao dịch không thể hoàn tác", HttpStatus.BAD_REQUEST),
    DUPLICATE_TRANSACTION("TRX_011", "Duplicate transaction detected", "Phát hiện giao dịch trùng lặp", HttpStatus.CONFLICT),
    TRANSACTION_EXPIRED("TRX_012", "Transaction has expired", "Giao dịch đã hết hạn", HttpStatus.BAD_REQUEST),

    // Account related errors
    SOURCE_ACCOUNT_NOT_FOUND("ACC_001", "Source account not found", "Không tìm thấy tài khoản nguồn", HttpStatus.NOT_FOUND),
    DESTINATION_ACCOUNT_NOT_FOUND("ACC_002", "Destination account not found", "Không tìm thấy tài khoản đích", HttpStatus.NOT_FOUND),
    ACCOUNT_FROZEN("ACC_003", "Account is frozen", "Tài khoản bị đóng băng", HttpStatus.BAD_REQUEST),
    ACCOUNT_CLOSED("ACC_004", "Account is closed", "Tài khoản đã đóng", HttpStatus.BAD_REQUEST),
    ACCOUNT_BLOCKED("ACC_005", "Account is blocked", "Tài khoản bị khóa", HttpStatus.BAD_REQUEST),
    SAME_ACCOUNT_TRANSFER("ACC_006", "Cannot transfer to same account", "Không thể chuyển khoản cùng tài khoản", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCOUNT_ACCESS("ACC_007", "Unauthorized account access", "Truy cập tài khoản không được phép", HttpStatus.FORBIDDEN),

    // External service errors
    ACCOUNT_SERVICE_UNAVAILABLE("EXT_001", "Account service unavailable", "Dịch vụ tài khoản không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    CORE_BANKING_SERVICE_ERROR("EXT_002", "Core banking service error", "Lỗi dịch vụ ngân hàng lõi", HttpStatus.SERVICE_UNAVAILABLE),
    NOTIFICATION_SERVICE_ERROR("EXT_003", "Notification service error", "Lỗi dịch vụ thông báo", HttpStatus.SERVICE_UNAVAILABLE),

    // Validation errors
    VALIDATION_ERROR("VAL_001", "Validation failed", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("VAL_002", "Required field is missing", "Thiếu trường bắt buộc", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_NUMBER("VAL_003", "Invalid account number", "Số tài khoản không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CURRENCY("VAL_004", "Invalid currency", "Loại tiền tệ không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("VAL_005", "Invalid date range", "Khoảng thời gian không hợp lệ", HttpStatus.BAD_REQUEST),

    // Database related errors
    DATABASE_CONNECTION_ERROR("DB_001", "Database connection failed", "Lỗi kết nối cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_OPERATION_FAILED("DB_002", "Database operation failed", "Thao tác cơ sở dữ liệu thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    OPTIMISTIC_LOCK_ERROR("DB_003", "Optimistic lock error", "Lỗi khóa lạc quan", HttpStatus.CONFLICT),

    // General errors
    INTERNAL_SERVER_ERROR("GEN_001", "Internal server error", "Lỗi máy chủ nội bộ", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("GEN_002", "Unauthorized access", "Truy cập không được phép", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("GEN_003", "Access forbidden", "Truy cập bị cấm", HttpStatus.FORBIDDEN),
    BAD_REQUEST("GEN_004", "Bad request", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST);

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
