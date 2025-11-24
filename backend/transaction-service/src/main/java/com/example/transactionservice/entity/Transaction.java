package com.example.transactionservice.entity;

import com.example.transactionservice.entity.enums.TransactionStatus;
import com.example.transactionservice.entity.enums.TransactionType;
import jakarta.persistence.*;
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
public class Transaction {
    @Id
    @UuidGenerator
    private String transactionId;

    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String description;
    private String referenceNumber;

    @CreationTimestamp
    private LocalDateTime transactionDate;

    private String createdBy; // customerId
}
