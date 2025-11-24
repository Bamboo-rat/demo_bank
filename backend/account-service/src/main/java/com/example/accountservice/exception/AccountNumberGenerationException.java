package com.example.accountservice.exception;

public class AccountNumberGenerationException extends BaseException {
    public AccountNumberGenerationException() {
        super(ErrorCode.ACCOUNT_NUMBER_GENERATION_FAILED);
    }

    public AccountNumberGenerationException(String message) {
        super(ErrorCode.ACCOUNT_NUMBER_GENERATION_FAILED, message);
    }

    public AccountNumberGenerationException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_NUMBER_GENERATION_FAILED, message, cause);
    }

    public AccountNumberGenerationException(int maxRetries) {
        super(ErrorCode.ACCOUNT_NUMBER_GENERATION_FAILED,
              String.format("Không thể sinh số tài khoản không trùng sau %d lần thử", maxRetries));
    }
}