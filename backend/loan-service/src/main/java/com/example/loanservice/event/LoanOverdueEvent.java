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
public class LoanOverdueEvent implements Serializable {
    private String eventId;
    private String loanAccountId;
    private String cifId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String scheduleId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private Integer daysOverdue;
    private BigDecimal overdueAmount;
    private BigDecimal principalOverdue;
    private BigDecimal interestOverdue;
    private BigDecimal penaltyAmount;
    private BigDecimal totalDue;
    private LocalDateTime eventTime;
}
