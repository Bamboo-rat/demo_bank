package com.example.accountservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "credit_accounts")
@PrimaryKeyJoinColumn(name = "accountId")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditAccount extends Account {

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal creditLimit; // Hạn mức tín dụng

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal availableCredit; // Hạn mức khả dụng

    @Column(nullable = false)
    private Integer statementDate; // Ngày sao kê hàng tháng (ví dụ: 25)

    @Column(nullable = false)
    private Integer paymentDueDate; // Ngày đến hạn thanh toán (ví dụ: 15 của tháng sau)
}
