package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.CustomerStatus;
import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cif_master",
        indexes = {
                @Index(name = "idx_cif_number", columnList = "cif_number", unique = true),
                @Index(name = "idx_username", columnList = "username", unique = true),
                @Index(name = "idx_national_id", columnList = "national_id", unique = true),
                @Index(name = "idx_customer_status", columnList = "customer_status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CIF_Master {
    @Id
    @UuidGenerator
    @Column(name = "cif_id", updatable = false, nullable = false)
    private String cifId;

    @Column(name = "cif_number", unique = true, nullable = false, length = 20)
    private String cifNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "national_id", unique = true, nullable = false)
    private String nationalId;

    private String nationality;

    private LocalDate issueDateNationalId;

    private String placeOfIssueNationalId;

    private String occupation;

    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus customerStatus = CustomerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Version
    @Column(name = "version")
    private Long version;

}
