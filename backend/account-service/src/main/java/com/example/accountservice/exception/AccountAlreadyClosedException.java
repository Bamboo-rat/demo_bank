package com.example.accountservice.exception;

import java.util.Map;

public class AccountAlreadyClosedException extends BaseException {

    public AccountAlreadyClosedException(String message) {
        super(ErrorCode.ACCOUNT_ALREADY_CLOSED, message);
    }

    public AccountAlreadyClosedException(String message, Map<String, Object> details) {
        super(ErrorCode.ACCOUNT_ALREADY_CLOSED, message, details);
    }

    public AccountAlreadyClosedException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_ALREADY_CLOSED, message, cause);
    }

    public AccountAlreadyClosedException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.ACCOUNT_ALREADY_CLOSED, message, details, cause);
    }
}