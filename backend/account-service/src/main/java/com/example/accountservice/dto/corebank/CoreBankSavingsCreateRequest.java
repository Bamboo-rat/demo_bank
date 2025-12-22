package com.example.accountservice.dto.corebank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request để tạo savings account trong Core Banking System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreBankSavingsCreateRequest {
    
    private String savingsAccountId; // UUID từ account-service
    private String customerId;
    private String cifNumber;
    private String sourceAccountNumber;
    
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    
    private String tenor; // e.g., "SIX_MONTHS"
    private Integer tenorMonths;
    private String interestPaymentMethod;
    private String autoRenewType;
    
    private LocalDate startDate;
    private LocalDate maturityDate;
    
    private String description;
}
