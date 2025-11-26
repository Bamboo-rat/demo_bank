package com.example.customerservice.exception;

public class RegistrationRateLimitException extends BaseException {

    public RegistrationRateLimitException(String message) {
        super(ErrorCode.OTP_ALREADY_SENT, message);
    }
}
