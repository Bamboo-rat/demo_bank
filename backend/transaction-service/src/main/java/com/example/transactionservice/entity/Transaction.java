package com.example.transactionservice.entity;

import com.example.transactionservice.entity.enums.FeePaymentMethod;
import com.example.transactionservice.entity.enums.TransactionStatus;
import com.example.transactionservice.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_source_account", columnList = "source_account_id"),
        @Index(name = "idx_dest_account", columnList = "destination_account_id"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @UuidGenerator
    private String transactionId;

    private String sourceAccountId;
    private String destinationAccountId;
    private String destinationBankCode;  // Bank code for interbank transfers
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionStatus status;

    private String description;
    private String referenceNumber;

    @CreationTimestamp
    private LocalDateTime transactionDate;

    private String createdBy;

    @Column(length = 20)
    private String transferType;  // "INTERNAL" or "INTERBANK"

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FeePaymentMethod feePaymentMethod;  // SOURCE or DESTINATION

    private BigDecimal fee;  // Transfer fee amount
}
