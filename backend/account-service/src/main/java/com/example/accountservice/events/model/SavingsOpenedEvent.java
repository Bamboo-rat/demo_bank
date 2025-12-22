package com.example.accountservice.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Kafka event khi mở sổ tiết kiệm mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsOpenedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String savingsAccountId;
    private String customerId;
    private String sourceAccountNumber;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private String tenor; // e.g., "SIX_MONTHS"
    private LocalDate startDate;
    private LocalDate maturityDate;
    private LocalDateTime timestamp;
    
    // For notification message
    private String message;
}
