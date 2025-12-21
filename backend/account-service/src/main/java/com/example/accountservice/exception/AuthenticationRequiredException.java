package com.example.accountservice.exception;

public class AuthenticationRequiredException extends BaseException {

    public AuthenticationRequiredException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public AuthenticationRequiredException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED, message, cause);
    }
}
