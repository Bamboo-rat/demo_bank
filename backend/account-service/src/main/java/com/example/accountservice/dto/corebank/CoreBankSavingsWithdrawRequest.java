package com.example.accountservice.dto.corebank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request để rút trước hạn savings trong Core Banking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreBankSavingsWithdrawRequest {
    
    private String savingsAccountId;
    private String sourceAccountNumber;
    
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    
    private LocalDateTime withdrawnAt;
    private String reason;
}
