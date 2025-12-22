package com.example.accountservice.exception;

/**
 * Exception cho nghiệp vụ savings không hợp lệ
 */
public class InvalidSavingsOperationException extends RuntimeException {
    
    private final String errorCode;
    
    public InvalidSavingsOperationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // Các error code chuẩn
    public static final String INSUFFICIENT_BALANCE = "SAVINGS_INSUFFICIENT_BALANCE";
    public static final String INVALID_TENOR = "SAVINGS_INVALID_TENOR";
    public static final String ACCOUNT_NOT_ACTIVE = "SAVINGS_ACCOUNT_NOT_ACTIVE";
    public static final String PREMATURE_WITHDRAWAL_NOT_ALLOWED = "SAVINGS_PREMATURE_WITHDRAWAL_NOT_ALLOWED";
    public static final String MINIMUM_AMOUNT_NOT_MET = "SAVINGS_MINIMUM_AMOUNT_NOT_MET";
}
