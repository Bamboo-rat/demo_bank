package com.example.loanservice.entity.enums;

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
     * Indicates whether the loan currently expects periodic payments.
     */
    public boolean isActive() {
        return requiresPayment;
    }

    /**
     * Signals that no further status transitions are allowed.
     */
    public boolean isTerminal() {
        return isFinal;
    }

    /**
     * Returns true when the loan is ready for disbursement.
     */
    public boolean canDisburse() {
        return this == APPROVED;
    }

    /**
     * Determines whether the current status supports cancellation.
     */
    public boolean canCancel() {
        return this == PENDING || this == IN_REVIEW;
    }

    /**
     * Flags statuses that need operational attention.
     */
    public boolean requiresAttention() {
        return this == OVERDUE || this == DEFAULTED;
    }

    /**
     * Used to block new loan applications for risky customers.
     */
    public boolean allowsNewLoanApplication() {
        return this != OVERDUE && this != DEFAULTED;
    }

    /**
     * True if money can be collected for this loan.
     */
    public boolean canAcceptPayment() {
        return requiresPayment;
    }
}
