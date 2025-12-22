package com.example.loanservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trạng thái đơn xin vay
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    /**
     * Đang chờ duyệt
     */
    PENDING_APPROVAL("application.status.pending_approval.name", "application.status.pending_approval.description", false),
    
    /**
     * Đang xử lý
     */
    UNDER_REVIEW("application.status.under_review.name", "application.status.under_review.description", false),
    
    /**
     * Yêu cầu bổ sung thông tin
     */
    REQUEST_MORE_INFO("application.status.request_more_info.name", "application.status.request_more_info.description", false),
    
    /**
     * Đã duyệt
     */
    APPROVED("application.status.approved.name", "application.status.approved.description", true),
    
    /**
     * Từ chối
     */
    REJECTED("application.status.rejected.name", "application.status.rejected.description", true),
    
    /**
     * Đã hủy bởi khách hàng
     */
    CANCELLED("application.status.cancelled.name", "application.status.cancelled.description", true);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isFinal;

    /**
     * Check if status is terminal (cannot be changed)
     * @return true if status is permanent
     */
    public boolean isTerminal() {
        return isFinal;
    }

    /**
     * Check if application can be reviewed
     * @return true if application can be processed
     */
    public boolean canBeReviewed() {
        return this == PENDING_APPROVAL || this == UNDER_REVIEW || this == REQUEST_MORE_INFO;
    }

    /**
     * Check if application is completed
     * @return true if application is in final state
     */
    public boolean isCompleted() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }
}
