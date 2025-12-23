package com.example.accountservice.dto.savings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response chứa thông tin chi tiết tài khoản tiết kiệm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccountResponse {

    private String savingsAccountId;
    private String savingsAccountNumber;
    private String customerId;
    private String customerName;
    private String sourceAccountNumber;
    private String beneficiaryAccountNumber;
    
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal estimatedInterest;
    private BigDecimal totalAmount;
    
    private String tenor;
    private Integer tenorMonths;
    private String tenorLabel;
    private String interestPaymentMethod;
    private String autoRenewType;
    
    private LocalDateTime startDate;
    private LocalDateTime maturityDate;
    
    private String status;
    private String description;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Số ngày còn lại đến ngày đáo hạn
    private Long daysUntilMaturity;
}
