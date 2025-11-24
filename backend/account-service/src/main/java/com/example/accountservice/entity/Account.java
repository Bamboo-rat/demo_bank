package com.example.accountservice.entity;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Account {
    @Id
    @UuidGenerator
    @Column(name = "account_id")
    private String accountId;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber; // Số tài khoản do ngân hàng cấp cho người dùng

    @Column(nullable = false, updatable = false)
    private String customerId; // UUID của khách hàng từ Customer Service

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType; // Loại tài khoản (Thanh toán, Tiết kiệm, Tín dụng)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status; // Trạng thái (Hoạt động, Tạm khóa, Đã đóng)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency; // Loại tiền tệ

    @Column(nullable = false, updatable = false)
    private LocalDateTime openedDate; // Ngày mở tài khoản

    private LocalDateTime closedDate; // Ngày đóng tài khoản

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
