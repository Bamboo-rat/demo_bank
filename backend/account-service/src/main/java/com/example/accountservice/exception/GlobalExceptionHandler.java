package com.example.accountservice.exception;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.environment:prod}")
    private String environment;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .userMessage(ex.getErrorCode().getVietnameseMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .method(extractMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .stackTrace(shouldIncludeStackTrace() ? getStackTrace(ex) : null)
                .build();

        log.error("BaseException [{}]: {} - {}", traceId, ex.getErrorCode().getCode(), ex.getMessage(), ex);

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }


    @ExceptionHandler(AccountCreationException.class)
    public ResponseEntity<ErrorResponse> handleAccountCreationException(AccountCreationException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(AccountAlreadyClosedException.class)
    public ResponseEntity<ErrorResponse> handleAccountAlreadyClosedException(AccountAlreadyClosedException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(AccountHasBalanceException.class)
    public ResponseEntity<ErrorResponse> handleAccountHasBalanceException(AccountHasBalanceException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(OutstandingCreditException.class)
    public ResponseEntity<ErrorResponse> handleOutstandingCreditException(OutstandingCreditException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(OutstandingLoanException.class)
    public ResponseEntity<ErrorResponse> handleOutstandingLoanException(OutstandingLoanException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(UnauthorizedAccountAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccountAccessException(UnauthorizedAccountAccessException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(AccountLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleAccountLimitExceededException(AccountLimitExceededException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(InsufficientInitialDepositException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientInitialDepositException(InsufficientInitialDepositException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(InvalidCustomerException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCustomerException(InvalidCustomerException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(InactiveCustomerException.class)
    public ResponseEntity<ErrorResponse> handleInactiveCustomerException(InactiveCustomerException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(KYCNotCompletedException.class)
    public ResponseEntity<ErrorResponse> handleKYCNotCompletedException(KYCNotCompletedException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(CoreBankingIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleCoreBankingIntegrationException(CoreBankingIntegrationException ex, WebRequest request) {
        return handleBaseException(ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .userMessage(ErrorCode.VALIDATION_ERROR.getVietnameseMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .method(extractMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .fieldErrors(fieldErrors)
                .stackTrace(shouldIncludeStackTrace() ? getStackTrace(ex) : null)
                .build();

        log.error("Validation error [{}]: {}", traceId, fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .userMessage(ErrorCode.VALIDATION_ERROR.getVietnameseMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .method(extractMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .stackTrace(shouldIncludeStackTrace() ? getStackTrace(ex) : null)
                .build();

        log.error("IllegalArgumentException [{}]: {}", traceId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .userMessage(ErrorCode.INTERNAL_SERVER_ERROR.getVietnameseMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .method(extractMethod(request))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .details(Map.of("exception", ex.getClass().getSimpleName()))
                .stackTrace(shouldIncludeStackTrace() ? getStackTrace(ex) : null)
                .build();

        log.error("Unhandled exception [{}]: {}", traceId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String extractMethod(WebRequest request) {
        try {
            return request.getHeader("X-HTTP-Method-Override") != null
                ? request.getHeader("X-HTTP-Method-Override")
                : "GET";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private boolean shouldIncludeStackTrace() {
        return "dev".equalsIgnoreCase(environment) || "development".equalsIgnoreCase(environment);
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}