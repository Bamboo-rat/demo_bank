package com.example.loanservice.dto.corebanking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreLoanDisbursementRequest {
    private String loanServiceRef;
    private String cifId;
    private String accountId;
    private BigDecimal disbursementAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private String notes;
}
