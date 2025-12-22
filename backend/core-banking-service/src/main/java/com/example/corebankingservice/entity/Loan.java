package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.LoanStatus;
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

@Entity
@Table(name = "loan", indexes = {
    @Index(name = "idx_loan_service_ref", columnList = "loan_service_ref"),
    @Index(name = "idx_cif_status", columnList = "cif_id, status"),
    @Index(name = "idx_account", columnList = "account_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @UuidGenerator
    @Column(name = "loan_id", length = 36)
    private String loanId;

    /**
     * Reference đến Loan Service (loan_id từ Loan Service)
     */
    @Column(name = "loan_service_ref", nullable = false, unique = true)
    private String loanServiceRef;

    @Column(name = "cif_id", nullable = false)
    private String cifId;

    /**
     * Số tài khoản khoản vay (14 số, prefix 40)
     */
    @Column(name = "loan_number", unique = true, length = 14)
    private String loanNumber;

    /**
     * Tài khoản thanh toán/giải ngân
     */
    @Column(name = "account_id", nullable = false)
    private String accountId;

    /**
     * Số tiền đã giải ngân
     */
    @Column(name = "disbursed_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal disbursedAmount;

    /**
     * Dư nợ gốc còn lại (balance)
     */
    @Column(name = "outstanding_principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal;

    /**
     * Tổng lãi đã trả
     */
    @Column(name = "total_interest_paid", precision = 19, scale = 2)
    private BigDecimal totalInterestPaid;

    /**
     * Tổng phạt đã trả
     */
    @Column(name = "total_penalty_paid", precision = 19, scale = 2)
    private BigDecimal totalPenaltyPaid;

    /**
     * Lãi suất (snapshot, chỉ để reference)
     */
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    /**
     * Kỳ hạn (snapshot, chỉ để reference)
     */
    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
