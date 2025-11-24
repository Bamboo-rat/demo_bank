package com.example.accountservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountNumberStatus {
    AVAILABLE(
        "account.number.status.available.name",
        "account.number.status.available.description"
    ),
    RESERVED(
        "account.number.status.reserved.name",
        "account.number.status.reserved.description"
    ),
    ASSIGNED(
        "account.number.status.assigned.name",
        "account.number.status.assigned.description"
    );

    private final String nameMessageCode;
    private final String descriptionMessageCode;

    /**
     * Check if account number can be assigned
     * @return true if available for assignment
     */
    public boolean canBeAssigned() {
        return this == AVAILABLE || this == RESERVED;
    }

    /**
     * Check if account number is in use
     * @return true if already assigned
     */
    public boolean isInUse() {
        return this == ASSIGNED;
    }

    /**
     * Check if reservation has expired (business logic placeholder)
     * This would typically check against a timestamp
     * @return true if status allows reuse
     */
    public boolean canBeReleased() {
        return this == RESERVED;
    }
}
