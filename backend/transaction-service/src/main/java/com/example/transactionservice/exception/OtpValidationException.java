package com.example.transactionservice.exception;

/**
 * Exception for OTP validation failures
 */
public class OtpValidationException extends BaseException {
    
    public OtpValidationException(String message) {
        super(ErrorCode.INVALID_OTP, message);
    }
    
    public OtpValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
