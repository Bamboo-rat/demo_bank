package com.example.transactionservice.entity.enums;

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
     * Kiểm tra xem loại giao dịch này có làm tăng số dư không
     * @return true nếu loại giao dịch làm tăng số dư
     */
    public boolean isIncoming() {
        return increasesBalance;
    }

    /**
     * Kiểm tra xem loại giao dịch này có làm giảm số dư không
     * @return true nếu loại giao dịch làm giảm số dư
     */
    public boolean isOutgoing() {
        return decreasesBalance;
    }
}
