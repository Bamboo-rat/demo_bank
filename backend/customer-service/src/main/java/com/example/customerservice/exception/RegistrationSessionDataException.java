package com.example.customerservice.exception;

import java.util.Map;

public class RegistrationSessionDataException extends BaseException {

    public RegistrationSessionDataException(String fieldName) {
        super(ErrorCode.REGISTRATION_SESSION_DATA_INCOMPLETE, Map.of(
                "missingField", fieldName
        ));
    }
}
