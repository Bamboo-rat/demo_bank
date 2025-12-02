package com.example.accountservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    CHECKING(
        "account.type.checking.name",
        "account.type.checking.description",
        new BigDecimal("2000"),      // 2K VND minimum balance
        new BigDecimal("500000000"),  // 500M VND daily limit
        true                           // Free transfers
    ),
    SAVINGS(
        "account.type.savings.name",
        "account.type.savings.description",
        new BigDecimal("100000"),     // 100K VND minimum balance
        new BigDecimal("200000000"),  // 200M VND daily limit
        false                          // No free transfers
    ),
    CREDIT(
        "account.type.credit.name",
        "account.type.credit.description",
        BigDecimal.ZERO,               // No minimum balance
        new BigDecimal("1000000000"), // 1B VND daily limit
        false                          // No free transfers
    );

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final BigDecimal minimumBalance;
    private final BigDecimal dailyTransactionLimit;
    private final boolean hasFreeTransfers;

    /**
     * Check if account type allows overdraft
     * @return true if overdraft is allowed
     */
    public boolean allowsOverdraft() {
        return this == CREDIT;
    }

    /**
     * Check if account type earns interest
     * @return true if interest is earned
     */
    public boolean earnsInterest() {
        return this == SAVINGS;
    }

    /**
     * Check if account can make withdrawals
     * @return true if withdrawals are allowed
     */
    public boolean allowsWithdrawal() {
        return this == CHECKING || this == SAVINGS;
    }

    /**
     * Check if transaction amount is within daily limit
     * @param amount Transaction amount
     * @return true if within limit
     */
    public boolean isWithinDailyLimit(BigDecimal amount) {
        return amount.compareTo(dailyTransactionLimit) <= 0;
    }
}
