package com.example.accountservice.exception;

import java.util.Map;

public class UnauthorizedAccountAccessException extends BaseException {

    public UnauthorizedAccountAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, message);
    }

    public UnauthorizedAccountAccessException(String message, Map<String, Object> details) {
        super(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, message, details);
    }

    public UnauthorizedAccountAccessException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, message, cause);
    }

    public UnauthorizedAccountAccessException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, message, details, cause);
    }
}