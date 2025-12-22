package com.example.loanservice.entity;

import com.example.loanservice.entity.enums.ApplicationStatus;
import com.example.loanservice.entity.enums.LoanPurpose;
import com.example.loanservice.entity.enums.RepaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Đơn xin vay
 * Lưu thông tin đăng ký vay, chưa có hợp đồng
 */
@Entity
@Table(name = "loan_application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @UuidGenerator
    @Column(name = "application_id")
    private String applicationId;
    /**
     * Reference to approved loan account ID
     */
    @Column(name = "loan_id")
    private String loanId;
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "tenor", nullable = false)
    private Integer tenor; // Số tháng

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private LoanPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_method", nullable = false)
    private RepaymentMethod repaymentMethod;

    @Column(name = "monthly_income", precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(name = "collateral_info", length = 500)
    private String collateralInfo;

    /**
     * Kết quả scoring (mock)
     */
    @Column(name = "scoring_result")
    private Integer scoringResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
