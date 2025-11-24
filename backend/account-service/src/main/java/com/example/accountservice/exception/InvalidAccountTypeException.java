package com.example.accountservice.exception;

public class InvalidAccountTypeException extends BaseException {
    public InvalidAccountTypeException() {
        super(ErrorCode.INVALID_ACCOUNT_TYPE);
    }

    public InvalidAccountTypeException(int accountType) {
        super(ErrorCode.INVALID_ACCOUNT_TYPE,
              String.format("Loại tài khoản %d không hợp lệ. Phải nằm trong khoảng [1..4]", accountType));
    }

    public InvalidAccountTypeException(String message) {
        super(ErrorCode.INVALID_ACCOUNT_TYPE, message);
    }
}