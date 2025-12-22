package com.example.accountservice.entity.enums;

/**
 * Trạng thái tài khoản tiết kiệm
 */
public enum SavingsAccountStatus {
    ACTIVE("Đang hoạt động"),
    MATURED("Đã đáo hạn"),
    CLOSED("Đã đóng"),
    CANCELLED("Đã hủy");

    private final String label;

    SavingsAccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
