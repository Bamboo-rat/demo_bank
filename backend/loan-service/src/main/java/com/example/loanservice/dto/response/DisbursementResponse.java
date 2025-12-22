package com.example.loanservice.dto.response;

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
public class DisbursementResponse {
    private String loanAccountId;
    private String transactionId;
    private BigDecimal disbursedAmount;
    private String accountId;
    private LocalDateTime disbursementTime;
    private String status;
    private String message;
}
