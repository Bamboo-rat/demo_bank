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
 * Kafka event khi sổ tiết kiệm đáo hạn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsMaturedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String savingsAccountId;
    private String customerId;
    private String sourceAccountNumber;
    private BigDecimal principalAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalAmount;
    private LocalDate maturityDate;
    private LocalDateTime timestamp;
    
    // Auto renew settings
    private String autoRenewType; // "NONE", "PRINCIPAL_ONLY", "PRINCIPAL_AND_INTEREST"
    
    // For notification message
    private String message;
}
