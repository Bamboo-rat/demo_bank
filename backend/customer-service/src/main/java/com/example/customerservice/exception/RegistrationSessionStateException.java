package com.example.customerservice.exception;

import java.util.Map;

public class RegistrationSessionStateException extends BaseException {

    public RegistrationSessionStateException(String expected, String actual) {
        super(ErrorCode.REGISTRATION_SESSION_INVALID_STATE, Map.of(
                "expected", expected,
                "actual", actual
        ));
    }
}
