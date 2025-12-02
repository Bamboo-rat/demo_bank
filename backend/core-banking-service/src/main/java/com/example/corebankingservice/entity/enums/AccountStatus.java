package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus {
    ACTIVE("account.status.active.name", "account.status.active.description"),
    DORMANT("account.status.dormant.name", "account.status.dormant.description"),
    FROZEN("account.status.frozen.name", "account.status.frozen.description"),
    BLOCKED("account.status.blocked.name", "account.status.blocked.description"),
    CLOSED("account.status.closed.name", "account.status.closed.description");

    private final String nameMessageCode;
    private final String descriptionMessageCode;

    /**
     * Check if account can perform transactions
     * @return true if account status allows transactions
     */
    public boolean canPerformTransactions() {
        return this == ACTIVE;
    }

    /**
     * Check if account status is terminal (cannot be changed)
     * @return true if status is permanent
     */
    public boolean isTerminal() {
        return this == CLOSED || this == BLOCKED;
    }

    /**
     * Check if account can receive deposits
     * @return true if deposits are allowed
     */
    public boolean canReceiveDeposits() {
        return this == ACTIVE || this == DORMANT;
    }

    /**
     * Check if account requires reactivation
     * @return true if account needs to be reactivated
     */
    public boolean requiresReactivation() {
        return this == DORMANT || this == FROZEN;
    }
}

