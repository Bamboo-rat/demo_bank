package com.example.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * Entity for tracking used Digital OTP tokens
 * Implements replay protection by storing validated transaction IDs
 */
@Entity
@Table(name = "digital_otp_used_tokens", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transactionId", unique = true),
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_validated_at", columnList = "validatedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalOtpUsedToken {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, unique = true)
    private String transactionId; // Nonce - prevents replay

    @Column(nullable = false)
    private String tokenHash; // SHA-256 hash of the token for additional verification

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime validatedAt;

    @Column(nullable = false)
    private Long clientTimestamp; // Original timestamp from client

    // Cleanup old records after 7 days
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
