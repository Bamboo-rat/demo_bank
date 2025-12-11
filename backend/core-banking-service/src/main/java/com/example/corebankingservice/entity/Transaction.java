package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.Currency;
import com.example.corebankingservice.entity.enums.TransactionChannel;
import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.entity.enums.TransactionType;
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
@Table(name = "transaction", indexes = {
    @Index(name = "idx_source_account", columnList = "source_account_id"),
    @Index(name = "idx_destination_account", columnList = "destination_account_id"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_trace_id", columnList = "trace_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @UuidGenerator
    @Column(name = "transaction_id", length = 36)
    private String transactionId;

    @Column(name = "source_account_id", length = 20)
    private String sourceAccountId; // Tài khoản nguồn

    @Column(name = "destination_account_id", length = 20)
    private String destinationAccountId; // Tài khoản đích

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount; // Số tiền giao dịch

    @Column(name = "balance_before", precision = 19, scale = 4)
    private BigDecimal balanceBefore; // Số dư trước giao dịch

    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter; // Số dư sau giao dịch

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO; // Phí phát sinh

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private Currency currency = Currency.VND; // Loại tiền tệ

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionChannel channel; // Kênh thực hiện

    @Column(name = "approved_by", length = 50)
    private String approvedBy; // Người phê duyệt

    @Column(name = "teller_id", length = 50)
    private String tellerId; // Mã giao dịch viên

    @Column(name = "bill_number", length = 50)
    private String billNumber; // Mã hóa đơn (cho thanh toán)

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber; // Mã hóa đơn VAT

    @Column(name = "debit_account", length = 20)
    private String debitAccount; // Tài khoản Nợ

    @Column(name = "credit_account", length = 20)
    private String creditAccount; // Tài khoản Có

    @Column(name = "journal_entry_id", length = 36)
    private String journalEntryId; // ID bút toán liên quan

    @Column(name = "trace_id", unique = true, length = 50)
    private String traceId; // Mã đối chiếu giữa các hệ thống

    @Column(name = "reference_number", length = 50)
    private String referenceNumber; // Mã tham chiếu

    @Column(name = "external_transaction_id", length = 50)
    private String externalTransactionId; // ID giao dịch từ hệ thống bên ngoài

    @Column(length = 500)
    private String description; // Mô tả giao dịch

    @Column(length = 1000)
    private String notes; // Ghi chú nội bộ

    @CreationTimestamp
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate; 

    @Column(name = "value_date")
    private LocalDateTime valueDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate; // Ngày hoàn thành

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;
}
