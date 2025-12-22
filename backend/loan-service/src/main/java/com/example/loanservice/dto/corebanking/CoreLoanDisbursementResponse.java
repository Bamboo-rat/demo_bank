package com.example.loanservice.dto.corebanking;

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
public class CoreLoanDisbursementResponse {
    private String loanId;
    private String loanServiceRef;
    private String transactionId;
    private BigDecimal disbursedAmount;
    private BigDecimal balanceAfter;
    private String status;
    private String message;
    private LocalDateTime disbursementTime;
}
