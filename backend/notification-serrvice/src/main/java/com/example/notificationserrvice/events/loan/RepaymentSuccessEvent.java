package com.example.notificationserrvice.events.loan;

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
public class RepaymentSuccessEvent implements Serializable {
    private String eventId;
    private String loanAccountId;
    private String cifId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String paymentHistoryId;
    private String scheduleId;
    private Integer installmentNumber;
    private BigDecimal paidAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal outstandingPrincipal;
    private String transactionId;
    private LocalDateTime paymentTime;
    private LocalDateTime eventTime;
}
