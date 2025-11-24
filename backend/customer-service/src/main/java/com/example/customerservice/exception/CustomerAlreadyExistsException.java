package com.example.customerservice.exception;

import java.util.Map;

public class CustomerAlreadyExistsException extends BaseException {
    public CustomerAlreadyExistsException(String field, String value) {
        super(getErrorCode(field),
                field + ": " + value,
                Map.of(field, value));
    }

    public CustomerAlreadyExistsException(String message) {
        super(ErrorCode.CUSTOMER_ALREADY_EXISTS, message, Map.of());
    }

    private static ErrorCode getErrorCode(String field) {
        return switch (field) {
            case "nationalId" -> ErrorCode.DUPLICATE_NATIONAL_ID;
            case "phoneNumber" -> ErrorCode.DUPLICATE_PHONE_NUMBER;
            case "email" -> ErrorCode.DUPLICATE_EMAIL;
            default -> ErrorCode.CUSTOMER_ALREADY_EXISTS;
        };
    }
}
