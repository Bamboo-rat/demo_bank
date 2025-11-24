package com.example.accountservice.exception;

import java.util.Map;

public class OutstandingCreditException extends BaseException {

    public OutstandingCreditException(String message) {
        super(ErrorCode.OUTSTANDING_CREDIT, message);
    }

    public OutstandingCreditException(String message, Map<String, Object> details) {
        super(ErrorCode.OUTSTANDING_CREDIT, message, details);
    }

    public OutstandingCreditException(String message, Throwable cause) {
        super(ErrorCode.OUTSTANDING_CREDIT, message, cause);
    }

    public OutstandingCreditException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.OUTSTANDING_CREDIT, message, details, cause);
    }
}