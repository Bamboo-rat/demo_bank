package com.example.corebankingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferExecutionRequest {

    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    private String destinationBankCode; 

    private String transferType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private BigDecimal fee;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference; // Reference from Transaction Service

    private String description;

    private String performedBy;
}
