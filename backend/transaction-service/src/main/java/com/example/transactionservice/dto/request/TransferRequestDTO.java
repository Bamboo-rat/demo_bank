package com.example.transactionservice.dto.request;

import com.example.transactionservice.entity.enums.FeePaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO to initiate transfer (Step 1: Trigger Digital OTP challenge)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDTO {

    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    private String destinationBankCode;  // Bank code for interbank transfers (null for internal)

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum transfer amount is 1,000 VND")
    private BigDecimal amount;

    private String description;

    private String beneficiaryName; // Optional: for display confirmation

    @NotNull(message = "Transfer type is required")
    private String transferType; // "INTERNAL" or "INTERBANK"

    @NotNull(message = "Fee payment method is required")
    private FeePaymentMethod feePaymentMethod; // SOURCE or DESTINATION
}
