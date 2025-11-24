package com.example.accountservice.exception;

public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException() {
        super(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(String message) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, message);
    }

    public AccountNotFoundException(String accountNumber, Object... args) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, args);
    }
}
