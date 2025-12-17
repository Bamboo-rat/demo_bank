package com.example.notificationserrvice.exception;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String userMessage;
    private final Map<String, Object> details;
    private final LocalDateTime timestamp;
    private final String traceId;

    protected BaseException(ErrorCode errorCode, String userMessage, Map<String, Object> details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage != null ? userMessage : errorCode.getMessage();
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.traceId = generateTraceId();
    }

    protected BaseException(ErrorCode errorCode, String userMessage, Map<String, Object> details) {
        this(errorCode, userMessage, details, null);
    }

    protected BaseException(ErrorCode errorCode, Map<String, Object> details) {
        this(errorCode, null, details, null);
    }

    protected BaseException(ErrorCode errorCode, String userMessage) {
        this(errorCode, userMessage, null, null);
    }

    protected BaseException(ErrorCode errorCode) {
        this(errorCode, null, null, null);
    }

    private String generateTraceId() {
        return "NOTIF_" + System.currentTimeMillis() + "_" + System.identityHashCode(Thread.currentThread());
    }
}
