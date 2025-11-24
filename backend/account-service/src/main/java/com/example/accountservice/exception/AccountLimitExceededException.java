package com.example.accountservice.exception;

import java.util.Map;

public class AccountLimitExceededException extends BaseException {

    public AccountLimitExceededException(String message) {
        super(ErrorCode.ACCOUNT_LIMIT_EXCEEDED, message);
    }

    public AccountLimitExceededException(String message, Map<String, Object> details) {
        super(ErrorCode.ACCOUNT_LIMIT_EXCEEDED, message, details);
    }

    public AccountLimitExceededException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_LIMIT_EXCEEDED, message, cause);
    }

    public AccountLimitExceededException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.ACCOUNT_LIMIT_EXCEEDED, message, details, cause);
    }
}