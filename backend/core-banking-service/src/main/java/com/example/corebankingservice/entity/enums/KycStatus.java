package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KycStatus {
    PENDING("kyc.status.pending.name", "kyc.status.pending.message", 0),
    SUBMITTED("kyc.status.submitted.name", "kyc.status.submitted.message", 1),
    IN_REVIEW("kyc.status.in_review.name", "kyc.status.in_review.message", 2),
    VERIFIED("kyc.status.verified.name", "kyc.status.verified.message", 3),
    REJECTED("kyc.status.rejected.name", "kyc.status.rejected.message", -1),
    EXPIRED("kyc.status.expired.name", "kyc.status.expired.message", -2),
    REQUIRES_ACTION("kyc.status.requires_action.name", "kyc.status.requires_action.message", 1);

    private final String nameMessageCode;
    private final String messageCode;

    /**
     * Priority level for processing (-2 lowest, 3 highest)
     * Mức độ ưu tiên xử lý (-2 thấp nhất, 3 cao nhất)
     */
    private final int priority;

    /**
     * Check if KYC is completed and verified
     * @return true if customer KYC is verified
     */
    public boolean isVerified() {
        return this == VERIFIED;
    }

    /**
     * Check if customer can open accounts
     * Only VERIFIED customers can open accounts
     * @return true if customer can open accounts
     */
    public boolean canOpenAccounts() {
        return this == VERIFIED;
    }

    /**
     * Check if customer can perform high-value transactions
     * @return true if allowed
     */
    public boolean canPerformHighValueTransactions() {
        return this == VERIFIED;
    }

    /**
     * Check if KYC status requires customer action
     * @return true if customer needs to take action
     */
    public boolean requiresCustomerAction() {
        return this == PENDING || this == REJECTED || this == EXPIRED || this == REQUIRES_ACTION;
    }

    /**
     * Check if KYC can be resubmitted
     * @return true if customer can resubmit KYC documents
     */
    public boolean canResubmit() {
        return this == REJECTED || this == EXPIRED || this == REQUIRES_ACTION;
    }
}
