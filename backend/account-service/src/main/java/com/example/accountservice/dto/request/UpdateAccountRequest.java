package com.example.accountservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating account information
 *
 * Supported updates:
 * - Credit limit (for credit accounts)
 * - Interest rate (for savings accounts)
 * - Term months (for savings accounts)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountRequest {

    // For Credit Accounts
    @DecimalMin(value = "0.00", message = "Credit limit must be positive")
    private BigDecimal creditLimit;

    // For Savings Accounts
    @DecimalMin(value = "0.00", message = "Interest rate must be positive")
    @Max(value = 100, message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    // For Savings Accounts
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 120, message = "Term cannot exceed 120 months")
    private Integer termMonths;
}
