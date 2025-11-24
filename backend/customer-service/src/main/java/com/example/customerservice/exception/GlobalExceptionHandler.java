package com.example.customerservice.exception;

import com.example.commonapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.environment:prod}")
    private String environment;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        // Use HttpStatus from ErrorCode
        HttpStatus status = ex.getErrorCode().getHttpStatus();

        log.error("Business exception occurred: {} - {} - Status: {}",
                ex.getErrorCode().getCode(), ex.getMessage(), status, ex);

        String localizedMessage = ex.getErrorCode().getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(localizedMessage)
                .userMessage(ex.getUserMessage() != null ? ex.getUserMessage() : localizedMessage)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(ex.getTimestamp())
                .traceId(getTraceId(ex))
                .details(ex.getDetails())
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing // Keep first error if multiple
                ));

        log.warn("Validation error: {}", fieldErrors);

        String localizedMessage = ErrorCode.VALIDATION_ERROR.getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(localizedMessage)
                .userMessage(localizedMessage)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .fieldErrors(fieldErrors)
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();

        // Use HttpStatus from ErrorCode
        HttpStatus status = ErrorCode.VALIDATION_ERROR.getHttpStatus();
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));

        log.warn("Constraint violation: {}", fieldErrors);

        String localizedMessage = ErrorCode.VALIDATION_ERROR.getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(localizedMessage)
                .userMessage(localizedMessage)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .fieldErrors(fieldErrors)
                .build();

        // Use HttpStatus from ErrorCode
        HttpStatus status = ErrorCode.VALIDATION_ERROR.getHttpStatus();
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        String localizedMessage = ErrorCode.CONSTRAINT_VIOLATION.getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.CONSTRAINT_VIOLATION.getCode())
                .message(localizedMessage)
                .userMessage(localizedMessage)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .details(Map.of("rootCause", ex.getMostSpecificCause().getMessage()))
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();

        // Use HttpStatus from ErrorCode
        HttpStatus status = ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus();
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        String localizedMessage = ErrorCode.INTERNAL_SERVER_ERROR.getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(localizedMessage)
                .userMessage(localizedMessage)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();

        // Use HttpStatus from ErrorCode
        HttpStatus status = ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus();
        return ResponseEntity.status(status).body(errorResponse);
    }

    private String getTraceId(BaseException ex) {
        // Priority: Exception traceId > Generate new
        String traceId = ex.getTraceId();
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateSimpleTraceId();
        }
        return traceId;
    }

    private String generateSimpleTraceId() {
        return "TR" + System.currentTimeMillis() + "_" + Thread.currentThread().threadId();
    }

    private boolean isDevelopment() {
        return "dev".equalsIgnoreCase(environment) || "development".equalsIgnoreCase(environment);
    }

    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}

