package com.example.corebankingservice.exception;

public class CifException extends BaseException {

    public CifException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CifException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public CifException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    // Specific CIF exception factory methods
    public static CifException cifNotFound(String cifNumber) {
        return new CifException(ErrorCode.CIF_NOT_FOUND, cifNumber);
    }

    public static CifException cifAlreadyExists(String cifNumber) {
        return new CifException(ErrorCode.CIF_ALREADY_EXISTS, cifNumber);
    }

    public static CifException usernameAlreadyExists(String username) {
        return new CifException(ErrorCode.USERNAME_ALREADY_EXISTS, username);
    }

    public static CifException nationalIdAlreadyExists(String nationalId) {
        return new CifException(ErrorCode.NATIONAL_ID_ALREADY_EXISTS, nationalId);
    }

    public static CifException invalidStatusTransition(String fromStatus, String toStatus) {
        return new CifException(ErrorCode.INVALID_CIF_STATUS_TRANSITION,
                String.format("từ %s sang %s", fromStatus, toStatus));
    }

    public static CifException invalidAction(String action) {
        return new CifException(ErrorCode.INVALID_CIF_ACTION, action);
    }

    public static CifException cifGenerationFailed() {
        return new CifException(ErrorCode.CIF_GENERATION_FAILED);
    }
}