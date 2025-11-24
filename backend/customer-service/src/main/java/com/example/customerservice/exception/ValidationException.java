package com.example.customerservice.exception;

import java.util.Map;

public class ValidationException extends BaseException {
    public ValidationException(Map<String, String> fieldErrors) {
        super(ErrorCode.VALIDATION_ERROR,
                "Dữ liệu đầu vào không hợp lệ",
                Map.of("fieldErrors", fieldErrors));
    }
}
