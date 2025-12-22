package com.example.accountservice.entity.enums;

/**
 * Loại tự động tái tục
 */
public enum AutoRenewType {
    NONE("Không tái tục"),
    PRINCIPAL_ONLY("Chỉ tái tục gốc"),
    PRINCIPAL_AND_INTEREST("Tái tục cả gốc và lãi");

    private final String label;

    AutoRenewType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
