package com.example.loanservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDisbursedEvent implements Serializable {
    private String eventId;
    private String loanAccountId;
    private String cifId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal disbursedAmount;
    private String accountId;
    private String transactionId;
    private LocalDateTime disbursementTime;
    private LocalDateTime eventTime;
}
