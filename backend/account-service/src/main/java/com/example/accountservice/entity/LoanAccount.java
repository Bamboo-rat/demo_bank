package com.example.accountservice.entity; // Sửa lại package nếu cần

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "loan_accounts")
@PrimaryKeyJoinColumn(name = "accountId")
@Data
@NoArgsConstructor
public class LoanAccount extends Account {

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount; // Số tiền vay gốc

    @Column(name = "interest_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal interestRate; // Lãi suất hàng năm (ví dụ: 0.125 tương đương 12.5%)

    @Column(name = "term_months", nullable = false)
    private Integer termMonths; // Kỳ hạn vay tính bằng tháng

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate; // Ngày giải ngân

    @Column(name = "monthly_installment", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyInstallment; // Số tiền trả góp hàng tháng
}