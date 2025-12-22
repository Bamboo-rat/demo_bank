package com.example.corebankingservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request dữ liệu khi account-service yêu cầu tất toán sổ trước hạn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsWithdrawalRequest {

    @NotBlank
    private String savingsAccountId;

    @NotBlank
    private String sourceAccountNumber;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal interestAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal penaltyAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal totalAmount;

    @NotNull
    private LocalDateTime withdrawnAt;

    private String reason;
}
