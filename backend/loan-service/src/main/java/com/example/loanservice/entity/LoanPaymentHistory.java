package com.example.loanservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử thanh toán khoản vay
 * Audit trail cho mọi giao dịch trả nợ
 */
@Entity
@Table(name = "loan_payment_history", indexes = {
    @Index(name = "idx_loan_payment", columnList = "loan_id, paid_date"),
    @Index(name = "idx_schedule_payment", columnList = "schedule_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentHistory {

    @Id
    @UuidGenerator
    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "loan_id", nullable = false)
    private String loanId;

    @Column(name = "schedule_id")
    private String scheduleId;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "principal_paid", precision = 19, scale = 2)
    private BigDecimal principalPaid;

    @Column(name = "interest_paid", precision = 19, scale = 2)
    private BigDecimal interestPaid;

    @Column(name = "penalty_paid", precision = 19, scale = 2)
    private BigDecimal penaltyPaid;

    /**
     * Reference đến Core Banking transaction (chỉ reference, không lưu balance)
     */
    @Column(name = "core_tx_ref", nullable = false)
    private String coreTxRef;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "result", nullable = false)
    private String result; // SUCCESS / FAILED

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "paid_date", nullable = false)
    private LocalDateTime paidDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
