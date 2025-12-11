package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for balance inquiry
 * Available Balance = Actual Balance - Hold Amount
 */
@Data
@Builder
public class BalanceResponse {

    private String accountNumber;
    private Currency currency;
    private BigDecimal balance;
    private BigDecimal holdAmount;
    private BigDecimal availableBalance; // = balance - holdAmount
    private LocalDateTime checkedAt;
}
