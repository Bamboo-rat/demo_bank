package com.example.accountservice.entity.enums;

/**
 * Tenor (Kỳ hạn) của tài khoản tiết kiệm
 */
public enum SavingsTenor {
    NO_TERM("Không kỳ hạn", 0),
    ONE_MONTH("1 tháng", 1),
    THREE_MONTHS("3 tháng", 3),
    SIX_MONTHS("6 tháng", 6),
    NINE_MONTHS("9 tháng", 9),
    TWELVE_MONTHS("12 tháng", 12),
    EIGHTEEN_MONTHS("18 tháng", 18),
    TWENTY_FOUR_MONTHS("24 tháng", 24),
    THIRTY_SIX_MONTHS("36 tháng", 36);

    private final String label;
    private final int months;

    SavingsTenor(String label, int months) {
        this.label = label;
        this.months = months;
    }

    public String getLabel() {
        return label;
    }

    public int getMonths() {
        return months;
    }
}
