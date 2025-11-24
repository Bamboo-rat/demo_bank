package com.example.accountservice.exception;

import java.util.Map;

public class InactiveCustomerException extends BaseException {

    public InactiveCustomerException(String message) {
        super(ErrorCode.INACTIVE_CUSTOMER, message);
    }

    public InactiveCustomerException(String message, Map<String, Object> details) {
        super(ErrorCode.INACTIVE_CUSTOMER, message, details);
    }

    public InactiveCustomerException(String message, Throwable cause) {
        super(ErrorCode.INACTIVE_CUSTOMER, message, cause);
    }

    public InactiveCustomerException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.INACTIVE_CUSTOMER, message, details, cause);
    }
}