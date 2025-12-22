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
public class CoreLoanRepaymentResponse {
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
