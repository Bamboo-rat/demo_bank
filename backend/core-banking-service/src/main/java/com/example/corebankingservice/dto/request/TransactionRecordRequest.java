package com.example.corebankingservice.dto.request;

import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.entity.enums.TransactionType;
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
public class TransactionRecordRequest {

    @NotBlank(message = "Source account ID is required")
    private String sourceAccountId;

    @NotBlank(message = "Destination account ID is required")
    private String destinationAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private BigDecimal fee;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Transaction status is required")
    private TransactionStatus status;

    @NotBlank(message = "Trace ID is required")
    private String traceId; // Reference number from Transaction Service

    private String description;

    private String createdBy;

    private BigDecimal sourceBalanceBefore;
    private BigDecimal sourceBalanceAfter;
    private BigDecimal destBalanceBefore;
    private BigDecimal destBalanceAfter;
}
