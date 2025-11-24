package com.example.accountservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClosureType {
    VOLUNTARY(
        "closure.type.voluntary.name",
        "closure.type.voluntary.description"
    ),
    DORMANT(
        "closure.type.dormant.name",
        "closure.type.dormant.description"
    ),
    COMPLIANCE(
        "closure.type.compliance.name",
        "closure.type.compliance.description"
    ),
    FRAUD(
        "closure.type.fraud.name",
        "closure.type.fraud.description"
    ),
    DECEASED(
        "closure.type.deceased.name",
        "closure.type.deceased.description"
    );

    private final String nameMessageCode;
    private final String descriptionMessageCode;

    /**
     * Check if closure type requires immediate action
     * @return true if immediate closure is required
     */
    public boolean requiresImmediateAction() {
        return this == FRAUD || this == COMPLIANCE;
    }

    /**
     * Check if closure is customer-initiated
     * @return true if customer requested closure
     */
    public boolean isCustomerInitiated() {
        return this == VOLUNTARY;
    }

    /**
     * Check if closure requires management approval
     * @return true if approval is needed
     */
    public boolean requiresApproval() {
        return this == FRAUD || this == DECEASED || this == COMPLIANCE;
    }
}