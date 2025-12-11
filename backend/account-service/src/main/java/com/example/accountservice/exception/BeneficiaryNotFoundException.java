package com.example.accountservice.exception;

public class BeneficiaryNotFoundException extends BaseException {
    public BeneficiaryNotFoundException() {
        super(ErrorCode.BENEFICIARY_NOT_FOUND);
    }

    public BeneficiaryNotFoundException(String message) {
        super(ErrorCode.BENEFICIARY_NOT_FOUND, message);
    }

    public BeneficiaryNotFoundException(String beneficiaryId, Object... args) {
        super(ErrorCode.BENEFICIARY_NOT_FOUND, args);
    }
}
