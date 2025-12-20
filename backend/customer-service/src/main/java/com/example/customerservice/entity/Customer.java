package com.example.customerservice.entity;

import com.example.customerservice.entity.enums.Gender;
import com.example.customerservice.entity.enums.KycStatus;
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
    @Index(name = "idx_cif_number", columnList = "cifNumber"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_phone_number", columnList = "phoneNumber")
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
    private String authProviderId;  // Keycloak user ID

    @Column(unique = true, nullable = false)
    private String cifNumber;  // Core Banking Customer ID

    @Column(nullable = false, unique = true)
    private String username;  // Phone number for login

    @Column(nullable = false)
    private String fullName;  // For display purposes only

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(nullable = false, unique = true)
    private String nationalId;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    // Digital OTP fields
    @Lob
    @Column(name = "digital_pin_hash", columnDefinition = "VARBINARY(255)")
    private byte[] digitalPinHash;

    @Lob
    @Column(name = "digital_public_key", columnDefinition = "TEXT")
    private String digitalPublicKey;

    @Column(name = "digital_otp_enabled")
    @Builder.Default
    private boolean digitalOtpEnabled = false;

    @Column(name = "digital_otp_failed_attempts")
    @Builder.Default
    private int digitalOtpFailedAttempts = 0;

    @Column(name = "digital_otp_locked_until")
    private LocalDateTime digitalOtpLockedUntil;

    @Column(name = "digital_otp_enrolled_at")
    private LocalDateTime digitalOtpEnrolledAt;

}
