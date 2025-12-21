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
    private String digitalOtpSecret; // Base64 encoded TOTP secret
    private String digitalPinHash;   // Base64-encoded hashed PIN (SHA-256 with salt)
    private String salt;             // Base64 encoded salt used for PIN hashing
}
