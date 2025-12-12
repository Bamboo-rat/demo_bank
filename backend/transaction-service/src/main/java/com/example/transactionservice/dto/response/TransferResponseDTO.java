package com.example.transactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transfer operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDTO {

    private String transactionId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String description;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String referenceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String message;

    // For OTP request response
    private Boolean otpSent;
    private String maskedPhoneNumber;
    private Integer otpExpiryMinutes;
}
