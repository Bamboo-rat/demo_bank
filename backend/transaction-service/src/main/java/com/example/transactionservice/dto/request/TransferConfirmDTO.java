package com.example.transactionservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferConfirmDTO {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Digital OTP token is required")
    private String digitalOtpToken;  // 6-digit TOTP token from client

    @NotBlank(message = "PIN hash is required for Digital OTP validation")
    private String pinHashCurrent;   // Hashed PIN for verification

    @NotNull(message = "Timestamp is required for Digital OTP validation")
    private Long timestamp;          // Client timestamp (time slice * 30000)
}
