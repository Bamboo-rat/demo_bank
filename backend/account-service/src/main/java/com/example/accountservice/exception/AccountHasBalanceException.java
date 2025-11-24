package com.example.accountservice.exception;

import java.util.Map;

public class AccountHasBalanceException extends BaseException {

    public AccountHasBalanceException(String message) {
        super(ErrorCode.ACCOUNT_HAS_BALANCE, message);
    }

    public AccountHasBalanceException(String message, Map<String, Object> details) {
        super(ErrorCode.ACCOUNT_HAS_BALANCE, message, details);
    }

    public AccountHasBalanceException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_HAS_BALANCE, message, cause);
    }

    public AccountHasBalanceException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.ACCOUNT_HAS_BALANCE, message, details, cause);
    }
}