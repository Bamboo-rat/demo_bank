package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoanStatus {
    PENDING("loan.status.pending.name", "loan.status.pending.description", false, false, 1),
    IN_REVIEW("loan.status.in_review.name", "loan.status.in_review.description", false, false, 2),
    APPROVED("loan.status.approved.name", "loan.status.approved.description", false, false, 3),
    REJECTED("loan.status.rejected.name", "loan.status.rejected.description", true, false, -1),
    DISBURSED("loan.status.disbursed.name", "loan.status.disbursed.description", false, true, 4),
    ACTIVE("loan.status.active.name", "loan.status.active.description", false, true, 5),
    OVERDUE("loan.status.overdue.name", "loan.status.overdue.description", false, true, 6),
    DEFAULTED("loan.status.defaulted.name", "loan.status.defaulted.description", false, true, 7),
    CLOSED("loan.status.closed.name", "loan.status.closed.description", true, false, 8),
    CANCELLED("loan.status.cancelled.name", "loan.status.cancelled.description", true, false, 0);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isFinal;
    private final boolean requiresPayment;
    private final int progressLevel;

    /**
     * Check if loan is active and requires payments
     * @return true if loan requires regular payments
     */
    public boolean isActive() {
        return requiresPayment;
    }

    /**
     * Check if loan status is terminal (cannot be changed)
     * @return true if status is final
     */
    public boolean isTerminal() {
        return isFinal;
    }

    /**
     * Check if loan can be disbursed
     * @return true if loan is approved and ready for disbursement
     */
    public boolean canDisburse() {
        return this == APPROVED;
    }

    /**
     * Check if loan can be cancelled
     * @return true if loan can be cancelled
     */
    public boolean canCancel() {
        return this == PENDING || this == IN_REVIEW;
    }

    /**
     * Check if loan requires attention (overdue or defaulted)
     * @return true if loan needs immediate action
     */
    public boolean requiresAttention() {
        return this == OVERDUE || this == DEFAULTED;
    }

    /**
     * Check if customer can apply for new loan
     * Customer cannot apply if they have OVERDUE or DEFAULTED loans
     * @return true if customer is eligible for new loans
     */
    public boolean allowsNewLoanApplication() {
        return this != OVERDUE && this != DEFAULTED;
    }
}
