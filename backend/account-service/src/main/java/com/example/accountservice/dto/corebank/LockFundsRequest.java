package com.example.accountservice.dto.corebank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để lock tiền trong Core Banking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockFundsRequest {
    
    private String accountNumber;
    private BigDecimal amount;
    private String lockType; // SAVINGS, COLLATERAL, HOLD
    private String referenceId; // Savings account ID
    private String description;
}
