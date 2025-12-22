package com.example.corebankingservice.dto.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response giải ngân khoản vay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDisbursementResponse {

    private String loanId;
    private String loanServiceRef;
    private String transactionId;
    private BigDecimal disbursedAmount;
    private BigDecimal balanceAfter;
    private String status;
    private String message;
    private LocalDateTime disbursementTime;
}
