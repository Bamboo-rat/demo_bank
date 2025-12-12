package com.example.transactionservice.dto.response;

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
public class BalanceResponse {

    private String accountNumber;
    private String currency; // VND, USD, EUR, etc.
    private BigDecimal balance;
    private BigDecimal holdAmount;
    private BigDecimal availableBalance; // = balance - holdAmount
    private LocalDateTime checkedAt;
}
