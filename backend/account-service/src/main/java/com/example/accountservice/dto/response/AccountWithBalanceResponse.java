package com.example.accountservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountWithBalanceResponse {
    
    // Account metadata
    private AccountResponse account;
    
    // Balance information from core-banking
    private BigDecimal availableBalance;
    private BigDecimal currentBalance;
    private BigDecimal holdAmount;
    
    // Additional financial info
    private BigDecimal creditLimit;      // For credit accounts
    private BigDecimal availableCredit;  // For credit accounts
    private BigDecimal interestEarned;   // For savings accounts
    
    // Timestamp of balance query
    private LocalDateTime balanceAsOf;
}
