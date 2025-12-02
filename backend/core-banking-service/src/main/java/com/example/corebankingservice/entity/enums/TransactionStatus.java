package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {
    PENDING("transaction.status.pending.name", "transaction.status.pending.description", false, false),
    PROCESSING("transaction.status.processing.name", "transaction.status.processing.description", false, false),
    COMPLETED("transaction.status.completed.name", "transaction.status.completed.description", true, false),
    FAILED("transaction.status.failed.name", "transaction.status.failed.description", true, true),
    CANCELLED("transaction.status.cancelled.name", "transaction.status.cancelled.description", true, false),
    REVERSED("transaction.status.reversed.name", "transaction.status.reversed.description", true, false);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isFinal;
    private final boolean isFailure;

    /**
     * Check if transaction is completed successfully
     * @return true if transaction is successfully completed
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * Check if transaction is in progress
     * @return true if transaction is still being processed
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * Check if transaction status is terminal (cannot be changed)
     * @return true if status is final
     */
    public boolean isTerminal() {
        return isFinal;
    }

    /**
     * Check if transaction can be retried
     * @return true if transaction can be attempted again
     */
    public boolean canRetry() {
        return this == FAILED;
    }

    /**
     * Check if transaction can be cancelled
     * @return true if transaction can be cancelled
     */
    public boolean canCancel() {
        return this == PENDING;
    }
}
