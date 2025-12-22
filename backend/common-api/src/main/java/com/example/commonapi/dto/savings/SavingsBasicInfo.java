package com.example.commonapi.dto.savings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO cơ bản của Savings Account để truyền qua Dubbo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsBasicInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String savingsAccountId;
    private String customerId;
    private String sourceAccountNumber;
    private BigDecimal principalAmount;
    private String status;
}
