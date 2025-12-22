package com.example.loanservice.dto.response;

import com.example.loanservice.entity.enums.LoanPurpose;
import com.example.loanservice.entity.enums.LoanStatus;
import com.example.loanservice.entity.enums.RepaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response thông tin hợp đồng khoản vay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountResponse {

    private String loanId;
    private String loanNumber;
    private String applicationId;
    private String customerId;
    private String customerName;
    
    private BigDecimal approvedAmount;
    private BigDecimal outstandingPrincipal;
    private BigDecimal interestRateSnapshot;
    private BigDecimal penaltyRate;
    
    private Integer tenor;
    private LoanPurpose purpose;
    private RepaymentMethod repaymentMethod;
    
    private LocalDate startDate;
    private LocalDate maturityDate;
    private LoanStatus status;
    
    private String disbursementAccount;
    private String repaymentAccount;
    private String disbursementTxRef;
    private LocalDateTime disbursementDate;
    
    private String notes;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin tính toán
    private Long daysRemaining;
    private Integer installmentsPaid;
    private Integer installmentsRemaining;
}
