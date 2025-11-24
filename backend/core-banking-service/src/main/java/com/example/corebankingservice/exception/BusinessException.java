package com.example.corebankingservice.exception;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public BusinessException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    // For backward compatibility with existing code
    public BusinessException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
}