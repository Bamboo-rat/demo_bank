package com.example.accountservice.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka event khi rút tiền trước hạn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsWithdrawnEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String savingsAccountId;
    private String customerId;
    private String sourceAccountNumber;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    private LocalDateTime withdrawnAt;
    
    // For notification message
    private String message;
}
