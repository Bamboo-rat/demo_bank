package com.example.customerservice.exception;

import java.util.Map;

public class AuthenticationException extends BaseException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }

    public AuthenticationException(ErrorCode errorCode, String userMessage, Throwable cause) {
        super(errorCode, userMessage, null, cause);
    }

    public AuthenticationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    // Backward compatibility constructors
    public AuthenticationException(String message) {
        super(ErrorCode.AUTHENTICATION_FAILED, message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(ErrorCode.AUTHENTICATION_FAILED, message, null, cause);
    }
}

