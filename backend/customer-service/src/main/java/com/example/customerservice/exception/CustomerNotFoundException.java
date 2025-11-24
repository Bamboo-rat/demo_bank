package com.example.customerservice.exception;

import java.util.Map;

public class CustomerNotFoundException extends BaseException {
    public CustomerNotFoundException(String customerId) {
        super(ErrorCode.CUSTOMER_NOT_FOUND,
                "Không tìm thấy khách hàng với ID: " + customerId,
                Map.of("customerId", customerId));
    }

    public CustomerNotFoundException(String field, String value) {
        super(ErrorCode.CUSTOMER_NOT_FOUND,
                "Không tìm thấy khách hàng với " + field + ": " + value,
                Map.of(field, value));
    }
}
