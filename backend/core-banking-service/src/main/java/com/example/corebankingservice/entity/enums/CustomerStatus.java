package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatus {

    ACTIVE("customer.status.active.name", "customer.status.active.description"),
    INACTIVE("customer.status.inactive.name", "customer.status.inactive.description"),
    SUSPENDED("customer.status.suspended.name", "customer.status.suspended.description"),
    BLOCKED("customer.status.blocked.name", "customer.status.blocked.description"),
    CLOSED("customer.status.closed.name", "customer.status.closed.description"),
    PENDING_APPROVAL("customer.status.pending_approval.name", "customer.status.pending_approval.description");
    private final String nameMessageCode;
    private final String descriptionMessageCode;

    /**
     * Check if customer can perform transactions
     * @return true if customer status allows transactions
     */
    public boolean canPerformTransactions() {
        return this == ACTIVE;
    }

    /**
     * Check if customer status is terminal (cannot be changed)
     * @return true if status is permanent
     */
    public boolean isTerminal() {
        return this == CLOSED || this == BLOCKED;
    }
}
