package com.example.loanservice.dto.response;

import com.example.loanservice.entity.enums.ApplicationStatus;
import com.example.loanservice.entity.enums.LoanPurpose;
import com.example.loanservice.entity.enums.RepaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response thông tin đơn xin vay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {

    private String applicationId;
    private String customerId;
    private BigDecimal requestedAmount;
    private Integer tenor;
    private LoanPurpose purpose;
    private RepaymentMethod repaymentMethod;
    private BigDecimal monthlyIncome;
    private String employmentStatus;
    private String collateralInfo;
    private Integer scoringResult;
    private ApplicationStatus status;
    private String rejectionReason;
    private String notes;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
