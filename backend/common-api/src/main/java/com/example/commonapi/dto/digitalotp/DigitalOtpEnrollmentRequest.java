package com.example.commonapi.dto.digitalotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for Digital OTP enrollment
 * Contains public key and PIN hash from client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalOtpEnrollmentRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerId;
    private String digitalPublicKey; // Base64 encoded public key
    private byte[] digitalPinHash;   // Hashed PIN using strong KDF (Argon2/BCrypt)
    private String salt;             // Salt used for PIN hashing
}
