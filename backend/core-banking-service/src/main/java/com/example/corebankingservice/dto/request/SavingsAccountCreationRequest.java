package com.example.corebankingservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body khi account-service yêu cầu tạo sổ tiết kiệm trong Core Banking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsAccountCreationRequest {

    @NotBlank
    private String savingsAccountId;

    @NotBlank
    private String customerId;

    private String cifNumber; // Optional - sẽ lấy từ sourceAccount nếu null

    @NotBlank
    private String sourceAccountNumber;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal principalAmount;

    @NotNull
    private BigDecimal interestRate;

    @NotBlank
    private String tenor;

    private Integer tenorMonths;

    @NotBlank
    private String interestPaymentMethod;

    @NotBlank
    private String autoRenewType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate maturityDate;

    private String description;
}
