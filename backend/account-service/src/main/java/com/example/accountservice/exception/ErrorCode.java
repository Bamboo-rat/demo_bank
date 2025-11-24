package com.example.accountservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Account related errors
    ACCOUNT_NOT_FOUND("ACC_001", "Account not found", "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS("ACC_002", "Account already exists", "Tài khoản đã tồn tại", HttpStatus.CONFLICT),
    INVALID_ACCOUNT_NUMBER("ACC_003", "Invalid account number", "Số tài khoản không hợp lệ", HttpStatus.BAD_REQUEST),
    ACCOUNT_NUMBER_GENERATION_FAILED("ACC_004", "Failed to generate unique account number", "Không thể tạo số tài khoản duy nhất", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ACCOUNT_TYPE("ACC_005", "Invalid account type", "Loại tài khoản không hợp lệ", HttpStatus.BAD_REQUEST),
    ACCOUNT_CREATION_FAILED("ACC_006", "Account creation failed", "Tạo tài khoản thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_ALREADY_CLOSED("ACC_007", "Account is already closed", "Tài khoản đã được đóng", HttpStatus.BAD_REQUEST),
    ACCOUNT_HAS_BALANCE("ACC_008", "Account has remaining balance", "Tài khoản còn số dư", HttpStatus.BAD_REQUEST),
    OUTSTANDING_CREDIT("ACC_009", "Account has outstanding credit", "Tài khoản còn dư nợ tín dụng", HttpStatus.BAD_REQUEST),
    OUTSTANDING_LOAN("ACC_010", "Account has outstanding loan", "Tài khoản còn dư nợ vay", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCOUNT_ACCESS("ACC_011", "Unauthorized account access", "Truy cập tài khoản không được phép", HttpStatus.FORBIDDEN),
    ACCOUNT_LIMIT_EXCEEDED("ACC_012", "Account limit exceeded", "Vượt quá giới hạn tài khoản", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_INITIAL_DEPOSIT("ACC_013", "Insufficient initial deposit", "Tiền gửi ban đầu không đủ", HttpStatus.BAD_REQUEST),

    // Customer related errors
    INVALID_CUSTOMER("CUS_001", "Invalid customer", "Khách hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    INACTIVE_CUSTOMER("CUS_002", "Customer is inactive", "Khách hàng không hoạt động", HttpStatus.BAD_REQUEST),
    KYC_NOT_COMPLETED("CUS_003", "KYC not completed", "Chưa hoàn thành xác thực danh tính", HttpStatus.BAD_REQUEST),

    // External service errors
    CORE_BANKING_INTEGRATION_ERROR("EXT_001", "Core banking integration error", "Lỗi tích hợp hệ thống ngân hàng lõi", HttpStatus.SERVICE_UNAVAILABLE),

    // Database related errors
    DATABASE_CONNECTION_ERROR("DB_001", "Database connection failed", "Lỗi kết nối cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_OPERATION_FAILED("DB_002", "Database operation failed", "Thao tác cơ sở dữ liệu thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    // Validation errors
    VALIDATION_ERROR("VAL_001", "Validation failed", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("VAL_002", "Required field is missing", "Thiếu trường bắt buộc", HttpStatus.BAD_REQUEST),

    // General errors
    INTERNAL_SERVER_ERROR("GEN_001", "Internal server error", "Lỗi máy chủ nội bộ", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("GEN_002", "Unauthorized access", "Truy cập không được phép", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("GEN_003", "Access forbidden", "Truy cập bị cấm", HttpStatus.FORBIDDEN);

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
