package com.example.accountservice.dto.savings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsProductResponse {
    private String productCode;
    private String productName;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer termMonths;
    private BigDecimal interestRate;
    private BigDecimal earlyWithdrawalPenalty;
}
