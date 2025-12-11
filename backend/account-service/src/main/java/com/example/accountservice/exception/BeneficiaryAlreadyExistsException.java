package com.example.accountservice.exception;

public class BeneficiaryAlreadyExistsException extends BaseException {
    public BeneficiaryAlreadyExistsException() {
        super(ErrorCode.BENEFICIARY_ALREADY_EXISTS);
    }

    public BeneficiaryAlreadyExistsException(String message) {
        super(ErrorCode.BENEFICIARY_ALREADY_EXISTS, message);
    }
}
