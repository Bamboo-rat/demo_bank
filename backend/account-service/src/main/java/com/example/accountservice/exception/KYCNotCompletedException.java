package com.example.accountservice.exception;

import java.util.Map;

public class KYCNotCompletedException extends BaseException {

    public KYCNotCompletedException(String message) {
        super(ErrorCode.KYC_NOT_COMPLETED, message);
    }

    public KYCNotCompletedException(String message, Map<String, Object> details) {
        super(ErrorCode.KYC_NOT_COMPLETED, message, details);
    }

    public KYCNotCompletedException(String message, Throwable cause) {
        super(ErrorCode.KYC_NOT_COMPLETED, message, cause);
    }

    public KYCNotCompletedException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.KYC_NOT_COMPLETED, message, details, cause);
    }
}