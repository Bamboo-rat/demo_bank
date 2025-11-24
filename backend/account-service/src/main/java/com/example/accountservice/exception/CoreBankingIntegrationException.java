package com.example.accountservice.exception;

import java.util.Map;

public class CoreBankingIntegrationException extends BaseException {

    public CoreBankingIntegrationException(String message) {
        super(ErrorCode.CORE_BANKING_INTEGRATION_ERROR, message);
    }

    public CoreBankingIntegrationException(String message, Map<String, Object> details) {
        super(ErrorCode.CORE_BANKING_INTEGRATION_ERROR, message, details);
    }

    public CoreBankingIntegrationException(String message, Throwable cause) {
        super(ErrorCode.CORE_BANKING_INTEGRATION_ERROR, message, cause);
    }

    public CoreBankingIntegrationException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.CORE_BANKING_INTEGRATION_ERROR, message, details, cause);
    }
}