package com.example.loanservice.entity;

import com.example.loanservice.entity.enums.LoanPurpose;
import com.example.loanservice.entity.enums.LoanStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccount {

    @Id
    @UuidGenerator
    @Column(name = "loan_id")
    private String loanId;

    @Column(name = "loan_number", unique = true, nullable = false)
    private String loanNumber;

    @Column(name = "application_id")
    private String applicationId;

    /**
     * Reference đến Core Banking Loan ID
     */
    @Column(name = "core_loan_id")
    private String coreLoanId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "approved_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    /**
     * Dư nợ gốc còn lại
     */
    @Column(name = "outstanding_principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal;

    /**
     * Lãi suất snapshot tại thời điểm duyệt
     */
    @Column(name = "interest_rate_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRateSnapshot;

    /**
     * Lãi suất phạt chậm trả
     */
    @Column(name = "penalty_rate", precision = 5, scale = 2)
    private BigDecimal penaltyRate;

    @Column(name = "tenor", nullable = false)
    private Integer tenor;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private LoanPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_method", nullable = false)
    private RepaymentMethod repaymentMethod;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    /**
     * Tài khoản thanh toán của khách hàng
     */
    @Column(name = "disbursement_account", nullable = false)
    private String disbursementAccount;

    /**
     * Tài khoản trả nợ (có thể khác với disbursement)
     */
    @Column(name = "repayment_account", nullable = false)
    private String repaymentAccount;

    /**
     * Reference đến Core Banking transaction (giải ngân)
     */
    @Column(name = "disbursement_tx_ref")
    private String disbursementTxRef;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
