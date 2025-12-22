package com.example.loanservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(LoanServiceException.class)
    public ResponseEntity<ErrorResponse> handleLoanServiceException(LoanServiceException ex) {
        log.error("[{}] {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .details(ex.getDetails())
                .build();
        
        return ResponseEntity.status(getHttpStatus(ex.getErrorCode()))
                .body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("[VALIDATION-ERROR] Request validation failed", ex);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code("GEN-002")
                .message("Invalid request parameters")
                .details(errors.toString())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("[UNHANDLED-ERROR] Unexpected error occurred", ex);
        
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code("GEN-001")
                .message("Internal server error")
                .details(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        String code = errorCode.getCode();
        
        if (code.startsWith("APP-") || code.startsWith("ACC-") || 
            code.startsWith("SCH-") || code.startsWith("PAY-")) {
            return HttpStatus.BAD_REQUEST;
        }
        
        if (code.startsWith("CORE-") || code.startsWith("CUST-") || code.startsWith("NOTIF-")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        
        if (code.equals("GEN-003")) {
            return HttpStatus.UNAUTHORIZED;
        }
        
        if (code.equals("GEN-004")) {
            return HttpStatus.NOT_FOUND;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
