package com.example.transactionservice.exception;

import java.math.BigDecimal;

public class TransactionLimitExceededException extends BaseException {
    public TransactionLimitExceededException() {
        super(ErrorCode.TRANSACTION_LIMIT_EXCEEDED);
    }

    public TransactionLimitExceededException(BigDecimal amount, BigDecimal limit) {
        super(ErrorCode.TRANSACTION_LIMIT_EXCEEDED,
              String.format("Transaction amount %s exceeds limit %s", amount, limit));
    }

    public TransactionLimitExceededException(String message) {
        super(ErrorCode.TRANSACTION_LIMIT_EXCEEDED, message);
    }
}
