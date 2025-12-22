package com.example.accountservice.entity;

import com.example.accountservice.entity.enums.AutoRenewType;
import com.example.accountservice.entity.enums.InterestPaymentMethod;
import com.example.accountservice.entity.enums.SavingsAccountStatus;
import com.example.accountservice.entity.enums.SavingsTenor;
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
 * Entity cho tài khoản tiết kiệm kỳ hạn (Fixed Deposit)
 * Khác với SavingsAccount thông thường - đây là tiền gửi có kỳ hạn, không rút trước hạn
 */
@Entity
@Table(name = "fixed_savings_accounts", indexes = {
        @Index(name = "idx_customer_id", columnList = "customerId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_maturity_date", columnList = "maturityDate"),
        @Index(name = "idx_source_account", columnList = "sourceAccountNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedSavingsAccount {

    @Id
    @UuidGenerator
    @Column(name = "savings_account_id")
    private String savingsAccountId;

    @Column(unique = true, length = 20)
    private String savingsAccountNumber;

    @Column(nullable = false, updatable = false)
    private String customerId;

    /**
     * Số tài khoản thanh toán nguồn (rút tiền từ đây để gửi tiết kiệm)
     */
    @Column(nullable = false, updatable = false, length = 20)
    private String sourceAccountNumber;

    /**
     * Số tài khoản nhận tiền lãi
     */
    @Column(length = 20)
    private String beneficiaryAccountNumber;

    /**
     * Số tiền gốc gửi tiết kiệm
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    /**
     * Lãi suất SNAPSHOT tại thời điểm mở sổ (không thay đổi sau này)
     */
    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal interestRate;

    /**
     * Kỳ hạn gửi
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SavingsTenor tenor;

    /**
     * Phương thức trả lãi
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterestPaymentMethod interestPaymentMethod;

    /**
     * Loại tự động gia hạn
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AutoRenewType autoRenewType;

    /**
     * Trạng thái tài khoản tiết kiệm
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SavingsAccountStatus status;

    /**
     * Ngày bắt đầu gửi
     */
    @Column(nullable = false, updatable = false)
    private LocalDate startDate;

    /**
     * Ngày đáo hạn
     */
    @Column(nullable = false)
    private LocalDate maturityDate;

    /**
     * Ghi chú/Mô tả
     */
    @Column(length = 500)
    private String description;

    /**
     * Tổng lãi đã trả (nếu trả lãi định kỳ)
     */
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalPaidInterest = BigDecimal.ZERO;

    /**
     * Ngày rút trước hạn (nếu có)
     */
    @Column
    private LocalDateTime withdrawnAt;

    /**
     * Số tiền phạt rút trước hạn (nếu có)
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal penaltyAmount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Tính số ngày còn lại đến ngày đáo hạn
     */
    public long getDaysUntilMaturity() {
        if (maturityDate == null) return 0;
        return LocalDate.now().until(maturityDate).getDays();
    }

    /**
     * Kiểm tra đã đáo hạn chưa
     */
    public boolean isMatured() {
        return maturityDate != null && LocalDate.now().isAfter(maturityDate);
    }

    /**
     * Kiểm tra có thể rút trước hạn không
     */
    public boolean canPrematureWithdraw() {
        return status == SavingsAccountStatus.ACTIVE && !isMatured();
    }
}
