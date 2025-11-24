package com.example.customerservice.exception;

import java.util.Map;

public class CustomerRegistrationException extends BaseException {
    public CustomerRegistrationException(String userMessage, Throwable cause) {
        super(ErrorCode.REGISTRATION_FAILED, userMessage, null, cause);
    }

    public CustomerRegistrationException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
        super(errorCode, null, details, cause);
    }
}
