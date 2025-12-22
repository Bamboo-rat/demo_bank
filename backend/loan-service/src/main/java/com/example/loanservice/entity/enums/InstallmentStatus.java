package com.example.loanservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trạng thái kỳ trả nợ
 */
@Getter
@RequiredArgsConstructor
public enum InstallmentStatus {
    /**
     * Chờ thanh toán
     */
    PENDING("installment.status.pending.name", "installment.status.pending.description", false),
    
    /**
     * Đã thanh toán
     */
    PAID("installment.status.paid.name", "installment.status.paid.description", true),
    
    /**
     * Quá hạn
     */
    OVERDUE("installment.status.overdue.name", "installment.status.overdue.description", false),
    
    /**
     * Trả một phần
     */
    PARTIALLY_PAID("installment.status.partially_paid.name", "installment.status.partially_paid.description", false),
    
    /**
     * Đã hủy
     */
    CANCELLED("installment.status.cancelled.name", "installment.status.cancelled.description", true);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isFinal;

    /**
     * Check if installment is completed
     * @return true if installment is fully paid or cancelled
     */
    public boolean isCompleted() {
        return isFinal;
    }

    /**
     * Check if installment requires payment
     * @return true if installment needs to be paid
     */
    public boolean requiresPayment() {
        return this == PENDING || this == OVERDUE || this == PARTIALLY_PAID;
    }

    /**
     * Check if payment can be made
     * @return true if installment accepts payment
     */
    public boolean canAcceptPayment() {
        return !isFinal;
    }
}
