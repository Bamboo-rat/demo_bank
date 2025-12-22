package com.example.accountservice.dto.savings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response khi rút trước hạn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrematureWithdrawResponse {

    private String savingsAccountId;
    private String transactionId;
    
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    
    private String destinationAccountNumber;
    private String message;
}
