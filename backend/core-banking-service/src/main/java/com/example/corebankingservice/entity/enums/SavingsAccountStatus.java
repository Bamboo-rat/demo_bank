package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trạng thái tài khoản tiết kiệm tại Core Banking
 */
@Getter
@RequiredArgsConstructor
public enum SavingsAccountStatus {
    ACTIVE("Đang hoạt động"),
    MATURED("Đã đáo hạn"),
    CLOSED("Đã đóng"),
    CANCELLED("Đã hủy");

    private final String label;
}
