package com.example.customerservice.entity;

import com.example.customerservice.entity.enums.CustomerStatus;
import com.example.customerservice.entity.enums.Gender;
import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.entity.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_auth_provider_id", columnList = "authProviderId"),
    @Index(name = "idx_core_banking_id", columnList = "coreBankingId"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_phone_number", columnList = "phoneNumber"),
    @Index(name = "idx_national_id", columnList = "nationalId"),
    @Index(name = "idx_kyc_status", columnList = "kycStatus"),
    @Index(name = "idx_customer_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @UuidGenerator
    private String customerId;

    @Column(nullable = false, unique = true)
    private String authProviderId;  // ID keycloak

    @Column(unique = true)
    private String coreBankingId; // ID core bank

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String nationality;

    @Column(nullable = false, unique = true)
    private String nationalId; // CCCD/CMND

    private LocalDate issueDateNationalId;

    private String placeOfIssueNationalId;

    private String occupation;

    private String position;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "permanent_street")),
            @AttributeOverride(name = "ward", column = @Column(name = "permanent_ward")),
            @AttributeOverride(name = "district", column = @Column(name = "permanent_district")),
            @AttributeOverride(name = "city", column = @Column(name = "permanent_city")),
            @AttributeOverride(name = "country", column = @Column(name = "permanent_country"))
    })
    private Address permanentAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "temporary_street")),
            @AttributeOverride(name = "ward", column = @Column(name = "temporary_ward")),
            @AttributeOverride(name = "district", column = @Column(name = "temporary_district")),
            @AttributeOverride(name = "city", column = @Column(name = "temporary_city")),
            @AttributeOverride(name = "country", column = @Column(name = "temporary_country"))
    })
    private Address temporaryAddress;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

}
