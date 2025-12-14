package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.Currency;
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
 * Audit log for balance operations
 */
@Entity
@Table(name = "balance_audit_logs", 
    indexes = {
        @Index(name = "idx_balance_audit_account", columnList = "account_number"),
        @Index(name = "idx_balance_audit_tx_ref", columnList = "transaction_reference"),
        @Index(name = "idx_balance_audit_time", columnList = "operation_time")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_transaction_reference", columnNames = "transaction_reference")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceAuditLog {

    @Id
    @UuidGenerator
    @Column(name = "audit_id")
    private String auditId;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "operation_type", nullable = false, length = 10)
    private String operationType; // DEBIT or CREDIT

    @Column(name = "previous_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal previousBalance;

    @Column(name = "operation_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal operationAmount;

    @Column(name = "new_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal newBalance;

    @Column(name = "hold_amount", precision = 19, scale = 2)
    private BigDecimal holdAmount;

    @Column(name = "available_balance", precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private Currency currency;

    @Column(name = "transaction_reference", nullable = false, length = 100)
    private String transactionReference;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @CreationTimestamp
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}
