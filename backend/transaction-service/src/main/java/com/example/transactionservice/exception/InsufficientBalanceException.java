package com.example.transactionservice.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BaseException {
    
    public InsufficientBalanceException(String message) {
        super(ErrorCode.INSUFFICIENT_BALANCE, message);
    }
    
    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE);
    }

    public InsufficientBalanceException(String accountNumber, BigDecimal balance, BigDecimal required) {
        super(ErrorCode.INSUFFICIENT_BALANCE,
              String.format("Insufficient balance in account %s. Balance: %s, Required: %s", 
                          accountNumber, balance, required));
    }
}
