package com.example.corebankingservice.dto.loan;

import com.example.corebankingservice.entity.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response thông tin khoản vay trong Core Banking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanInfoResponse {

    private String loanId;
    private String loanServiceRef;
    private String cifId;
    private String accountId;
    private BigDecimal disbursedAmount;
    private BigDecimal outstandingPrincipal;
    private BigDecimal totalInterestPaid;
    private BigDecimal totalPenaltyPaid;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private LoanStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
