package com.example.corebankingservice.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    // For backward compatibility with existing code
    public ResourceNotFoundException(String message) {
        super(ErrorCode.CIF_NOT_FOUND, message);
    }
}