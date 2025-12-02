package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    DEPOSIT("transaction.type.deposit.name", "transaction.type.deposit.description", true, false),
    WITHDRAWAL("transaction.type.withdrawal.name", "transaction.type.withdrawal.description", false, true),
    TRANSFER("transaction.type.transfer.name", "transaction.type.transfer.description", false, true),
    PAYMENT("transaction.type.payment.name", "transaction.type.payment.description", false, true),
    FEE("transaction.type.fee.name", "transaction.type.fee.description", false, true);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean increasesBalance;
    private final boolean decreasesBalance;

    /**
     * Check if transaction type increases account balance
     * @return true if this transaction adds money to account
     */
    public boolean isCredit() {
        return increasesBalance;
    }

    /**
     * Check if transaction type decreases account balance
     * @return true if this transaction removes money from account
     */
    public boolean isDebit() {
        return decreasesBalance;
    }

    /**
     * Check if transaction requires source account
     * @return true if transaction needs a source account
     */
    public boolean requiresSourceAccount() {
        return this == WITHDRAWAL || this == TRANSFER || this == PAYMENT || this == FEE;
    }

    /**
     * Check if transaction requires destination account
     * @return true if transaction needs a destination account
     */
    public boolean requiresDestinationAccount() {
        return this == DEPOSIT || this == TRANSFER;
    }
}
