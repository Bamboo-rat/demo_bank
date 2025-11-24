package com.example.accountservice.exception;

import java.util.Map;

public class InvalidCustomerException extends BaseException {

    public InvalidCustomerException(String message) {
        super(ErrorCode.INVALID_CUSTOMER, message);
    }

    public InvalidCustomerException(String message, Map<String, Object> details) {
        super(ErrorCode.INVALID_CUSTOMER, message, details);
    }

    public InvalidCustomerException(String message, Throwable cause) {
        super(ErrorCode.INVALID_CUSTOMER, message, cause);
    }

    public InvalidCustomerException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.INVALID_CUSTOMER, message, details, cause);
    }
}