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
    private String digitalOtpToken;  // Signed token from client

    @NotNull(message = "Timestamp is required for Digital OTP validation")
    private Long timestamp;          // Client timestamp for 30s window validation
}
