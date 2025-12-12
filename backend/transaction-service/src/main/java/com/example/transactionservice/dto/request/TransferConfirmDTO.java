package com.example.transactionservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to confirm transfer with OTP (Step 2: Verify OTP and Execute)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferConfirmDTO {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String otp;
}
