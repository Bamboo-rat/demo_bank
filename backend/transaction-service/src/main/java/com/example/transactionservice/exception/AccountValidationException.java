package com.example.transactionservice.exception;

/**
 * Exception for account validation failures
 */
public class AccountValidationException extends BaseException {
    
    public AccountValidationException(String message) {
        super(ErrorCode.ACCOUNT_VALIDATION_FAILED, message);
    }
    
    public AccountValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
