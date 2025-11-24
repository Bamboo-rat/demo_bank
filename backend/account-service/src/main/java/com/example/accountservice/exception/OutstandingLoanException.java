package com.example.accountservice.exception;

import java.util.Map;

public class OutstandingLoanException extends BaseException {

    public OutstandingLoanException(String message) {
        super(ErrorCode.OUTSTANDING_LOAN, message);
    }

    public OutstandingLoanException(String message, Map<String, Object> details) {
        super(ErrorCode.OUTSTANDING_LOAN, message, details);
    }

    public OutstandingLoanException(String message, Throwable cause) {
        super(ErrorCode.OUTSTANDING_LOAN, message, cause);
    }

    public OutstandingLoanException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.OUTSTANDING_LOAN, message, details, cause);
    }
}