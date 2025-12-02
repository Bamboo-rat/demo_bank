package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum RiskLevel {
    LOW(
        "risk.level.low.name",
        "risk.level.low.description",
        new BigDecimal("100000000"), // 100M VND daily limit
        1 // Green
    ),
    MEDIUM(
        "risk.level.medium.name",
        "risk.level.medium.description",
        new BigDecimal("50000000"), // 50M VND daily limit
        2 // Yellow/Orange
    ),
    HIGH(
        "risk.level.high.name",
        "risk.level.high.description",
        new BigDecimal("20000000"), // 20M VND daily limit
        3 // Orange
    ),
    CRITICAL(
        "risk.level.critical.name",
        "risk.level.critical.description",
        new BigDecimal("5000000"), // 5M VND daily limit
        4// Red
    ),
    BLACKLISTED(
        "risk.level.blacklisted.name",
        "risk.level.blacklisted.description",
        BigDecimal.ZERO, // No transactions allowed
        5 // Black
    );
    private final String nameMessageCode;
    private final String descriptionMessageCode;

    /**
     * Daily transaction limit in VND
     * Hạn mức giao dịch hàng ngày tính bằng VND
     */
    private final BigDecimal dailyTransactionLimit;

    /**
     * Risk severity level (1 = lowest, 5 = highest)
     * Mức độ nghiêm trọng rủi ro (1 = thấp nhất, 5 = cao nhất)
     */
    private final int severityLevel;

    /**
     * Check if customer can perform transactions
     * @return true if customer risk level allows transactions
     */
    public boolean canPerformTransactions() {
        return this != BLACKLISTED;
    }

    /**
     * Check if transaction amount is within daily limit
     * @param amount Transaction amount
     * @return true if within limit
     */
    public boolean isWithinDailyLimit(BigDecimal amount) {
        if (this == BLACKLISTED) {
            return false;
        }
        return amount.compareTo(dailyTransactionLimit) <= 0;
    }

    /**
     * Check if customer requires manual approval for transactions
     * @return true if manual approval required
     */
    public boolean requiresManualApproval() {
        return this == HIGH || this == CRITICAL || this == BLACKLISTED;
    }

    /**
     * Check if customer requires enhanced due diligence
     * @return true if enhanced monitoring required
     */
    public boolean requiresEnhancedDueDiligence() {
        return severityLevel >= 3;
    }

    /**
     * Get recommended review frequency in days
     * @return Number of days between reviews
     */
    public int getReviewFrequencyDays() {
        return switch (this) {
            case LOW -> 365;        // Annual review
            case MEDIUM -> 180;     // Semi-annual review
            case HIGH -> 90;        // Quarterly review
            case CRITICAL -> 30;    // Monthly review
            case BLACKLISTED -> 7;  // Weekly review
        };
    }
}
