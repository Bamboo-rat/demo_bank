package com.example.loanservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mục đích vay
 */
@Getter
@RequiredArgsConstructor
public enum LoanPurpose {
    /**
     * Vay tiêu dùng
     */
    CONSUMER_LOAN("loan.purpose.consumer_loan.name", "loan.purpose.consumer_loan.description", 25.0, 10000000L, 500000000L),
    
    /**
     * Vay mua nhà
     */
    HOME_LOAN("loan.purpose.home_loan.name", "loan.purpose.home_loan.description", 12.0, 100000000L, 5000000000L),
    
    /**
     * Vay mua xe
     */
    AUTO_LOAN("loan.purpose.auto_loan.name", "loan.purpose.auto_loan.description", 15.0, 50000000L, 2000000000L),
    
    /**
     * Vay tín chấp
     */
    PERSONAL_LOAN("loan.purpose.personal_loan.name", "loan.purpose.personal_loan.description", 20.0, 10000000L, 300000000L),
    
    /**
     * Vay kinh doanh
     */
    BUSINESS_LOAN("loan.purpose.business_loan.name", "loan.purpose.business_loan.description", 18.0, 50000000L, 10000000000L),
    
    /**
     * Vay giáo dục
     */
    EDUCATION_LOAN("loan.purpose.education_loan.name", "loan.purpose.education_loan.description", 10.0, 20000000L, 500000000L);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final Double maxInterestRate;
    private final Long minAmount;
    private final Long maxAmount;

    /**
     * Check if amount is within limits
     * @param amount loan amount
     * @return true if amount is valid
     */
    public boolean isAmountValid(Long amount) {
        return amount >= minAmount && amount <= maxAmount;
    }

    /**
     * Check if interest rate is within limits
     * @param rate interest rate
     * @return true if rate is valid
     */
    public boolean isInterestRateValid(Double rate) {
        return rate <= maxInterestRate;
    }
}
