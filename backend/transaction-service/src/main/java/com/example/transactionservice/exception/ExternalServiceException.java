package com.example.transactionservice.exception;

/**
 * Exception for external service communication errors
 */
public class ExternalServiceException extends BaseException {
    
    public ExternalServiceException(String message) {
        super(ErrorCode.ACCOUNT_SERVICE_UNAVAILABLE, message);
    }
    
    public ExternalServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public ExternalServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
