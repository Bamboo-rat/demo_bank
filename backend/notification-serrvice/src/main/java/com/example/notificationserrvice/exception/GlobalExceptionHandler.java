package com.example.notificationserrvice.exception;
import com.example.commonapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler cho Notification Service
 * Xử lý tất cả các exceptions và trả về response thống nhất
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.environment:prod}")
    private String environment;

    /**
     * Xử lý BaseException và các subclass của nó
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request) {
        
        HttpStatus status = ex.getErrorCode().getHttpStatus();
        
        log.error("Business exception occurred: {} - {} - Status: {}", 
                ex.getErrorCode().getCode(), ex.getMessage(), status, ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(ex.getTimestamp())
                .traceId(ex.getTraceId())
                .details(ex.getDetails())
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Xử lý validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));
        
        log.warn("Validation error at {}: {}", request.getRequestURI(), fieldErrors);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .userMessage("Vui lòng kiểm tra lại thông tin đầu vào")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .fieldErrors(fieldErrors)
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý Authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        log.error("Authentication error at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.UNAUTHORIZED.getCode())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .userMessage("Xác thực không thành công")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Xử lý Access Denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.error("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.FORBIDDEN.getCode())
                .message(ErrorCode.FORBIDDEN.getMessage())
                .userMessage("Bạn không có quyền truy cập tài nguyên này")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Xử lý IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.error("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_REQUEST.getCode())
                .message(ex.getMessage())
                .userMessage("Yêu cầu không hợp lệ")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Xử lý tất cả các exceptions chưa được xử lý
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .userMessage("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Xử lý RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.error("Runtime error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi")
                .userMessage("Đã xảy ra lỗi hệ thống")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(generateSimpleTraceId())
                .stackTrace(isDevelopment() ? getStackTrace(ex) : null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Kiểm tra môi trường development
     */
    private boolean isDevelopment() {
        return "dev".equalsIgnoreCase(environment) || "development".equalsIgnoreCase(environment);
    }
    
    /**
     * Lấy stack trace
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Generate simple trace ID
     */
    private String generateSimpleTraceId() {
        return "NOTIF_" + System.currentTimeMillis();
    }
}
