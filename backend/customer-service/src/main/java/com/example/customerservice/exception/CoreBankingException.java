package com.example.customerservice.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class CoreBankingException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public CoreBankingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public CoreBankingException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details != null ? details : Map.of();
    }

    public CoreBankingException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details != null ? details : Map.of();
    }

    public CoreBankingException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = Map.of();
    }
}