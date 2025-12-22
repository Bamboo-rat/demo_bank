package com.example.accountservice.entity.enums;

/**
 * Phương thức trả lãi
 */
public enum InterestPaymentMethod {
    END_OF_TERM("Trả lãi cuối kỳ"),
    MONTHLY("Trả lãi hàng tháng"),
    QUARTERLY("Trả lãi hàng quý"),
    BEGINNING("Trả lãi đầu kỳ");

    private final String label;

    InterestPaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
