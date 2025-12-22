package com.example.loanservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trạng thái khoản vay
 */
@Getter
@RequiredArgsConstructor
public enum LoanStatus {
    /**
     * Đang chờ duyệt
     */
    PENDING_APPROVAL("loan.status.pending_approval.name", "loan.status.pending_approval.description", false, false),
    
    /**
     * Đã duyệt, chưa giải ngân
     */
    APPROVED("loan.status.approved.name", "loan.status.approved.description", false, false),
    
    /**
     * Từ chối
     */
    REJECTED("loan.status.rejected.name", "loan.status.rejected.description", true, false),
    
    /**
     * Đang hoạt động (đã giải ngân)
     */
    ACTIVE("loan.status.active.name", "loan.status.active.description", false, true),
    
    /**
     * Quá hạn
     */
    OVERDUE("loan.status.overdue.name", "loan.status.overdue.description", false, true),
    
    /**
     * Đã tất toán
     */
    PAID_OFF("loan.status.paid_off.name", "loan.status.paid_off.description", true, false),
    
    /**
     * Nợ xấu
     */
    DEFAULT("loan.status.default.name", "loan.status.default.description", false, true),
    
    /**
     * Đã đóng
     */
    CLOSED("loan.status.closed.name", "loan.status.closed.description", true, false);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isFinal;
    private final boolean requiresPayment;

    /**
     * Check if loan is active and requires payments
     * @return true if loan requires regular payments
     */
    public boolean isActive() {
        return requiresPayment;
    }

    /**
     * Check if status is terminal (cannot be changed)
     * @return true if status is permanent
     */
    public boolean isTerminal() {
        return isFinal;
    }

    /**
     * Check if loan can be repaid
     * @return true if loan accepts payments
     */
    public boolean canAcceptPayment() {
        return this == ACTIVE || this == OVERDUE || this == DEFAULT;
    }
}
