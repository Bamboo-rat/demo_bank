package com.example.customerservice.exception;

import java.util.Map;

public class RegistrationSessionNotFoundException extends BaseException {

    public RegistrationSessionNotFoundException(String phoneNumber, String sessionId) {
        super(ErrorCode.REGISTRATION_SESSION_NOT_FOUND, Map.of(
                "phoneNumber", phoneNumber,
                "sessionId", sessionId
        ));
    }
}
