package com.example.corebankingservice.dto.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response trả nợ khoản vay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentResponse {

    private String loanId;
    private String loanServiceRef;
    private String transactionId;
    private BigDecimal paidAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    private BigDecimal outstandingPrincipal;
    private BigDecimal balanceAfter;
    private String status;
    private String message;
    private LocalDateTime repaymentTime;
}
