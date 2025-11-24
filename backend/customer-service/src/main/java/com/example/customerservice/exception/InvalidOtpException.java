package com.example.customerservice.exception;

public class InvalidOtpException extends BaseException {

    public InvalidOtpException(String message) {
        super(ErrorCode.INVALID_OTP, message);
    }

    public InvalidOtpException(String message, Throwable cause) {
        super(ErrorCode.INVALID_OTP, message, null, cause);
    }
}