package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @UuidGenerator
    private String transactionId;

    @Column(name = "source_account_id")
    private String sourceAccountId; // Tài khoản nguồn

    @Column(name = "destination_account_id")
    private String destinationAccountId; // Tài khoản đích

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    private LocalTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
