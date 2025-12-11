package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for balance operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceOperationResponse {

    private String accountNumber;
    private BigDecimal previousBalance;
    private BigDecimal operationAmount;
    private BigDecimal newBalance;
    private BigDecimal availableBalance;
    private Currency currency;
    private String transactionReference;
    private String operationType;  // DEBIT or CREDIT
    private LocalDateTime operationTime;
    private String message;
}
