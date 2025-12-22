package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.SavingsAccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core Banking record cho tài khoản tiết kiệm kỳ hạn.
 * Được đồng bộ từ account-service để đảm bảo dữ liệu nhất quán.
 */
@Entity
@Table(name = "savings_accounts", indexes = {
        @Index(name = "idx_savings_customer", columnList = "customerId"),
        @Index(name = "idx_savings_cif", columnList = "cifNumber"),
        @Index(name = "idx_savings_status", columnList = "status"),
        @Index(name = "idx_savings_source", columnList = "sourceAccountNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsAccount {

    @Id
    @Column(name = "savings_account_id", length = 36)
    private String savingsAccountId;

    @Column(unique = true, length = 20)
    private String savingsAccountNumber;

    @Column(nullable = false, length = 36)
    private String customerId;

    @Column(length = 20)
    private String cifNumber;

    @Column(nullable = false, length = 20)
    private String sourceAccountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal interestRate;

    @Column(nullable = false, length = 30)
    private String tenor;

    @Column
    private Integer tenorMonths;

    @Column(length = 30)
    private String interestPaymentMethod;

    @Column(length = 30)
    private String autoRenewType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SavingsAccountStatus status;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal paidInterestAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal penaltyAmount;

    @Column
    private LocalDateTime withdrawnAt;

    @Column(length = 36)
    private String fundLockId;

    @Column(length = 50)
    private String lastTransactionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
