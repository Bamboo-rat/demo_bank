package com.example.accountservice.exception;

import java.util.Map;

public class InsufficientInitialDepositException extends BaseException {

    public InsufficientInitialDepositException(String message) {
        super(ErrorCode.INSUFFICIENT_INITIAL_DEPOSIT, message);
    }

    public InsufficientInitialDepositException(String message, Map<String, Object> details) {
        super(ErrorCode.INSUFFICIENT_INITIAL_DEPOSIT, message, details);
    }

    public InsufficientInitialDepositException(String message, Throwable cause) {
        super(ErrorCode.INSUFFICIENT_INITIAL_DEPOSIT, message, cause);
    }

    public InsufficientInitialDepositException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.INSUFFICIENT_INITIAL_DEPOSIT, message, details, cause);
    }
}