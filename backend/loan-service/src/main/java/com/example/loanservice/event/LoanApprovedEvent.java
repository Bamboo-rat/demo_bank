package com.example.loanservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApprovedEvent implements Serializable {
    private String eventId;
    private String loanApplicationId;
    private String loanAccountId;
    private String cifId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LocalDate expectedDisbursementDate;
    private LocalDateTime eventTime;
}
