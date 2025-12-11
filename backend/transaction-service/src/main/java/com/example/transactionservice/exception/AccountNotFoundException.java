package com.example.transactionservice.exception;

public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException(String accountNumber) {
        super(ErrorCode.SOURCE_ACCOUNT_NOT_FOUND,
              String.format("Account not found: %s", accountNumber));
    }

    public AccountNotFoundException(ErrorCode errorCode, String accountNumber) {
        super(errorCode, String.format("Account not found: %s", accountNumber));
    }
}
