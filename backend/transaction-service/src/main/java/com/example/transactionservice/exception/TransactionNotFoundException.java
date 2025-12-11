package com.example.transactionservice.exception;

public class TransactionNotFoundException extends BaseException {
    public TransactionNotFoundException() {
        super(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    public TransactionNotFoundException(String transactionId) {
        super(ErrorCode.TRANSACTION_NOT_FOUND, 
              String.format("Transaction not found with ID: %s", transactionId));
    }
}
