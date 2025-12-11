package com.example.accountservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries", indexes = {
        @Index(name = "idx_beneficiary_customer", columnList = "customer_id"),
        @Index(name = "idx_beneficiary_account", columnList = "customer_id,beneficiary_account_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @UuidGenerator
    @Column(name = "beneficiary_id")
    private String beneficiaryId;

    @Column(name = "customer_id", nullable = false)
    private String customerId; // ID của khách hàng sở hữu danh bạ này

    @Column(name = "beneficiary_account_number", nullable = false, length = 20)
    private String beneficiaryAccountNumber; // Số tài khoản người thụ hưởng

    @Column(name = "beneficiary_name", nullable = false, length = 200)
    private String beneficiaryName; // Tên người thụ hưởng

    @Column(name = "bank_code", length = 20)
    private String bankCode; // Mã ngân hàng (null nếu là nội bộ)

    @Column(name = "bank_name", length = 200)
    private String bankName; // Tên ngân hàng (null nếu là nội bộ)

    @Column(name = "nickname", length = 100)
    private String nickname; // Biệt danh do người dùng đặt (VD: "Vợ", "Mẹ", "Bạn An")

    @Column(name = "note", length = 500)
    private String note; // Ghi chú thêm

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false; // Đã xác thực tài khoản người nhận chưa

    @Column(name = "transfer_count", nullable = false)
    @Builder.Default
    private Integer transferCount = 0; // Số lần chuyển khoản cho người này

    @Column(name = "last_transfer_date")
    private LocalDateTime lastTransferDate; // Lần chuyển tiền gần nhất

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
