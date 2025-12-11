package com.example.transactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for balance operations
 * Returned from Core Banking Service via Feign Client
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
    private String currency; // VND, USD, EUR, etc.
    private String transactionReference;
    private String operationType;  // DEBIT or CREDIT
    private LocalDateTime operationTime;
    private String message;
}
