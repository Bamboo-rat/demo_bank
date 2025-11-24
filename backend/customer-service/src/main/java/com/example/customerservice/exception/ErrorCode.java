package com.example.customerservice.exception;

import com.example.commonapi.util.MessageUtils;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // General errors (1000-1999)
    INTERNAL_SERVER_ERROR("E1000", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("E1001", "validation.invalid", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("E1002", "validation.invalid", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("E1003", "auth.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E1004", "auth.forbidden", HttpStatus.FORBIDDEN),

    // Customer errors (2000-2999)
    CUSTOMER_NOT_FOUND("E2000", "customer.not.found", HttpStatus.NOT_FOUND),
    CUSTOMER_ALREADY_EXISTS("E2001", "customer.already.exists", HttpStatus.CONFLICT),
    CUSTOMER_INACTIVE("E2002", "customer.inactive", HttpStatus.FORBIDDEN),
    CUSTOMER_BLOCKED("E2003", "customer.blocked", HttpStatus.FORBIDDEN),

    // Registration errors (2100-2199)
    REGISTRATION_FAILED("E2100", "customer.register.failed", HttpStatus.BAD_REQUEST),
    DUPLICATE_NATIONAL_ID("E2101", "customer.register.national.id.exists", HttpStatus.CONFLICT),
    DUPLICATE_PHONE_NUMBER("E2102", "customer.register.phone.exists", HttpStatus.CONFLICT),
    DUPLICATE_EMAIL("E2103", "customer.register.email.exists", HttpStatus.CONFLICT),
    INVALID_AGE("E2104", "validation.invalid", HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD("E2105", "validation.invalid", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("E2106", "validation.email.invalid", HttpStatus.BAD_REQUEST),
    INVALID_PHONE("E2107", "validation.phone.invalid", HttpStatus.BAD_REQUEST),
    INVALID_NATIONAL_ID("E2108", "validation.national.id.invalid", HttpStatus.BAD_REQUEST),
    INVALID_FULLNAME("E2109", "validation.invalid", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED("E2110", "system.error", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_OTP("E2111", "validation.invalid", HttpStatus.BAD_REQUEST),

    // Authentication errors (2200-2299)
    INVALID_CREDENTIALS("E2200", "auth.login.failed", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("E2201", "auth.login.failed", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("E2202", "auth.token.expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("E2203", "auth.token.invalid", HttpStatus.UNAUTHORIZED),

    // Keycloak errors (3000-3999)
    KEYCLOAK_USER_CREATION_FAILED("E3000", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    KEYCLOAK_USER_UPDATE_FAILED("E3001", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    KEYCLOAK_USER_DELETE_FAILED("E3002", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    KEYCLOAK_CONNECTION_ERROR("E3003", "system.unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // Core Banking errors (5000-5999)
    CORE_BANKING_CONNECTION_ERROR("E5000", "system.unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    CORE_BANKING_CREATE_CUSTOMER_FAILED("E5001", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    CORE_BANKING_DELETE_CUSTOMER_FAILED("E5002", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    CORE_BANKING_INVALID_RESPONSE("E5003", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    CORE_BANKING_SERVICE_UNAVAILABLE("E5004", "system.unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // Database errors (4000-4999)
    DATABASE_ERROR("E4000", "system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    CONSTRAINT_VIOLATION("E4001", "validation.invalid", HttpStatus.CONFLICT),
    DATA_INTEGRITY_ERROR("E4002", "system.error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String messageCode;
    private final HttpStatus httpStatus;

    private static MessageUtils messageUtils;

    ErrorCode(String code, String messageCode, HttpStatus httpStatus) {
        this.code = code;
        this.messageCode = messageCode;
        this.httpStatus = httpStatus;
    }

    public static void setMessageUtils(MessageUtils utils) {
        messageUtils = utils;
    }

    private static String resolveMessage(String code, Object... args) {
        if (messageUtils == null) {
            return code;
        }
        return args == null || args.length == 0
                ? messageUtils.getMessage(code)
                : messageUtils.getMessage(code, args);
    }

    public String getMessage() {
        return resolveMessage(this.messageCode);
    }

    public String getMessage(Object... args) {
        return resolveMessage(this.messageCode, args);
    }

    public String getMessageCode() {
        return messageCode;
    }
}

