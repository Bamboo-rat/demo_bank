package com.example.customerservice.exception;

public class OtpExpiredException extends BaseException {

    public OtpExpiredException(String message) {
        super(ErrorCode.OTP_EXPIRED, message);
    }
}
