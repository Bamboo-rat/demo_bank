package com.example.accountservice.exception;

public class UnauthorizedBeneficiaryAccessException extends BaseException {
    public UnauthorizedBeneficiaryAccessException() {
        super(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    public UnauthorizedBeneficiaryAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED_ACCESS, message);
    }
}
