package com.example.transactionservice.exception;

public class InvalidTransactionException extends BaseException {
    public InvalidTransactionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidTransactionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
