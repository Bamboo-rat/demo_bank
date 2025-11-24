package com.example.accountservice.dto.response;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Savings Account with specific fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SavingsAccountResponse {
    
    // Base account fields
    private String accountId;
    private String accountNumber;
    private String customerId;
    private AccountType accountType;
    private AccountStatus status;
    private Currency currency;
    private LocalDateTime openedDate;
    private LocalDateTime closedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String customerName;
    private String customerStatus;
    private String cifNumber;
    
    // Savings-specific fields
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal projectedInterest;
}
