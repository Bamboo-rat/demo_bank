package com.example.transactionservice.entity.enums;

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
     * Kiểm tra xem trạng thái này có phải là trạng thái cuối cùng không
     * @return true nếu đây là trạng thái cuối cùng (không thể chuyển sang trạng thái khác)
     */
    public boolean isTerminal() {
        return isFinal;
    }

    /**
     * Kiểm tra xem trạng thái này có thể chuyển sang trạng thái mới không
     * @return true nếu có thể chuyển trạng thái
     */
    public boolean canTransitionTo(TransactionStatus newStatus) {
        if (this.isFinal) {
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra xem giao dịch có thành công không
     * @return true nếu giao dịch hoàn thành thành công
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
}
