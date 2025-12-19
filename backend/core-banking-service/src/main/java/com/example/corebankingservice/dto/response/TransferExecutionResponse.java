package com.example.corebankingservice.dto.response;

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
public class TransferExecutionResponse {

    private String transactionId; // Core Banking transaction ID

    private String sourceAccountNumber;
    private String destinationAccountNumber;

    private BigDecimal amount;
    private BigDecimal fee;

    private BigDecimal sourceBalanceBefore;
    private BigDecimal sourceBalanceAfter;
    private BigDecimal sourceAvailableBalance;

    private BigDecimal destBalanceBefore;
    private BigDecimal destBalanceAfter;
    private BigDecimal destAvailableBalance;

    private String transactionReference;
    private String currency;

    private LocalDateTime executionTime;

    private String message;
}
