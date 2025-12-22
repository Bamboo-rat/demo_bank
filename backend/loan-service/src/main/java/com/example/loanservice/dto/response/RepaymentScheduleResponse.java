package com.example.loanservice.dto.response;

import com.example.loanservice.entity.enums.InstallmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response lịch trả góp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentScheduleResponse {

    private String scheduleId;
    private String loanId;
    private Integer installmentNo;
    private LocalDate dueDate;
    
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal penaltyAmount;
    
    private InstallmentStatus status;
    private LocalDateTime paidDate;
    private String paymentTxRef;
    private Integer overdueDays;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
