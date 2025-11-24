package com.example.corebankingservice.exception;

import com.example.commonapi.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.environment:production}")
    private String environment;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        log.error("BaseException occurred [traceId: {}]: {}", traceId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .userMessage(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .method(getHttpMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .details(createDetailsMap(ex))
                .stackTrace(isDevEnvironment() ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        log.error("Validation error occurred [traceId: {}]: {}", traceId, ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_REQUEST.getCode())
                .message(ErrorCode.INVALID_REQUEST.getMessage())
                .userMessage("Dữ liệu đầu vào không hợp lệ")
                .path(request.getDescription(false).replace("uri=", ""))
                .method(getHttpMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .fieldErrors(fieldErrors)
                .stackTrace(isDevEnvironment() ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        log.error("Unexpected error occurred [traceId: {}]: {}", traceId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .userMessage("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.")
                .path(request.getDescription(false).replace("uri=", ""))
                .method(getHttpMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .stackTrace(isDevEnvironment() ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createDetailsMap(BaseException ex) {
        Map<String, Object> details = new HashMap<>();
        if (ex.getDetails() != null) {
            details.put("details", ex.getDetails());
        }
        return details.isEmpty() ? null : details;
    }

    private String getHttpMethod(WebRequest request) {
        try {
            return request.getHeader("X-HTTP-Method-Override");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private boolean isDevEnvironment() {
        return "development".equalsIgnoreCase(environment) || "dev".equalsIgnoreCase(environment);
    }

    private String getStackTrace(Exception ex) {
        if (!isDevEnvironment()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}