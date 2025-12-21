package com.example.commonapi.dto.digitalotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request DTO for Digital OTP validation
 * Contains signed token and transaction payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalOtpValidationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerId;
    private String digitalOtpToken;      // 6-digit TOTP token from client
    private String pinHashCurrent;       // Current PIN hash for verification
    
    // Transaction payload fields (used for TOTP generation)
    private String transactionId;        // Nonce for replay protection
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String destinationBankCode;  // Bank code for interbank transfers
    private BigDecimal amount;
    private Long timestamp;              // Client timestamp (time slice * 30000)
}
