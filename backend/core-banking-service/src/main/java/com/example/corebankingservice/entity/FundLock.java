package com.example.corebankingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity để track các khoản tiền bị lock trong hệ thống
 * Dùng cho tiết kiệm, thế chấp, hold giao dịch, etc.
 */
@Entity
@Table(name = "fund_locks", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber"),
    @Index(name = "idx_reference_id", columnList = "referenceId"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundLock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String lockId;
    
    @Column(nullable = false, length = 20)
    private String accountNumber;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lockedAmount;
    
    @Column(nullable = false, length = 30)
    private String lockType; // SAVINGS, COLLATERAL, HOLD
    
    @Column(nullable = false, length = 100)
    private String referenceId; // Savings account ID, transaction ID, etc.
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, length = 20)
    private String status; // LOCKED, RELEASED
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime lockedAt;
    
    private LocalDateTime releasedAt;
    
    private String releaseReason;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
