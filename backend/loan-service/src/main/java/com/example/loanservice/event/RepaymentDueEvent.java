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
public class RepaymentDueEvent implements Serializable {
    private String eventId;
    private String loanAccountId;
    private String cifId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String scheduleId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal dueAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private Integer daysUntilDue;
    private LocalDateTime eventTime;
}
