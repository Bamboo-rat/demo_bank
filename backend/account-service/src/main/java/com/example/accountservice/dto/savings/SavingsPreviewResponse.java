package com.example.accountservice.dto.savings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response chứa thông tin preview tiết kiệm
 * Giúp khách hàng xem trước lãi suất, tiền lãi dự kiến, ngày đáo hạn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsPreviewResponse {

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    
    private String tenor;
    private Integer tenorMonths;
    private String interestPaymentMethod;
    
    private BigDecimal estimatedInterest;
    private BigDecimal totalAmount;
    
    private LocalDate startDate;
    private LocalDate maturityDate;
    private Long daysToMaturity;
    
    private String description;
}
