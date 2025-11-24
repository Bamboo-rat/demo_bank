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
@Table(name = "savings_accounts")
@PrimaryKeyJoinColumn(name = "accountId") // Liên kết với bảng cha qua khóa chính
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccount extends Account {

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal interestRate; // Lãi suất

    @Column(nullable = false)
    private Integer termMonths; // Kỳ hạn gửi (tính bằng tháng)
}
