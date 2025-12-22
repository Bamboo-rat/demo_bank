package com.example.loanservice.entity;

import com.example.loanservice.entity.enums.InstallmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lịch trả góp
 * Bảng sống để theo dõi, nhắc nợ, phạt chậm
 */
@Entity
@Table(name = "repayment_schedule", indexes = {
    @Index(name = "idx_loan_installment", columnList = "loan_id, installment_no"),
    @Index(name = "idx_due_date_status", columnList = "due_date, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentSchedule {

    @Id
    @UuidGenerator
    @Column(name = "schedule_id")
    private String scheduleId;

    @Column(name = "loan_id", nullable = false)
    private String loanId;

    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Gốc phải trả kỳ này
     */
    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    /**
     * Lãi phải trả kỳ này
     */
    @Column(name = "interest_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal interestAmount;

    /**
     * Tổng tiền = gốc + lãi
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Số tiền đã trả
     */
    @Column(name = "paid_amount", precision = 19, scale = 2)
    private BigDecimal paidAmount;

    /**
     * Tiền phạt (nếu quá hạn)
     */
    @Column(name = "penalty_amount", precision = 19, scale = 2)
    private BigDecimal penaltyAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InstallmentStatus status;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    /**
     * Reference đến Core Banking transaction
     */
    @Column(name = "payment_tx_ref")
    private String paymentTxRef;

    @Column(name = "overdue_days")
    private Integer overdueDays;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
