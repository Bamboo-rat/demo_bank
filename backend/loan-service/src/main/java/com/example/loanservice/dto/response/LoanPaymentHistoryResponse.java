package com.example.loanservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response lịch sử thanh toán
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentHistoryResponse {

    private String paymentId;
    private String loanId;
    private String scheduleId;
    
    private BigDecimal paidAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    
    private String coreTxRef;
    private String paymentMethod;
    private String result;
    private String notes;
    
    private LocalDateTime paidDate;
    private LocalDateTime createdAt;
}
