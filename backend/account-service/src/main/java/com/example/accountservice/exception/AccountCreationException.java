package com.example.accountservice.exception;

import java.util.Map;

public class AccountCreationException extends BaseException {

    public AccountCreationException(String message) {
        super(ErrorCode.ACCOUNT_CREATION_FAILED, message);
    }

    public AccountCreationException(String message, Map<String, Object> details) {
        super(ErrorCode.ACCOUNT_CREATION_FAILED, message, details);
    }

    public AccountCreationException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_CREATION_FAILED, message, cause);
    }

    public AccountCreationException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.ACCOUNT_CREATION_FAILED, message, details, cause);
    }
}