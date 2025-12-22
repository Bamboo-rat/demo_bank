package com.example.accountservice.exception;

/**
 * Exception cho savings account không tìm thấy
 */
public class SavingsAccountNotFoundException extends RuntimeException {
    
    private static final String ERROR_CODE = "SAVINGS_NOT_FOUND";
    
    public SavingsAccountNotFoundException(String savingsAccountId) {
        super(String.format("Savings account not found: %s", savingsAccountId));
    }
    
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
