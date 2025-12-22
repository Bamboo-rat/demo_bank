package com.example.corebankingservice.dto.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request trả nợ khoản vay
 * Gọi từ Loan Service khi khách hàng trả nợ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentRequest {

    @NotBlank(message = "Loan service reference không được để trống")
    private String loanServiceRef;

    @NotBlank(message = "Account ID không được để trống")
    private String accountId;

    @NotNull(message = "Số tiền trả nợ không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền trả nợ phải > 0")
    private BigDecimal amount;

    @NotNull(message = "Số tiền gốc không được để trống")
    private BigDecimal principalAmount;

    @NotNull(message = "Số tiền lãi không được để trống")
    private BigDecimal interestAmount;

    private BigDecimal penaltyAmount;

    /**
     * Reference từ Loan Service schedule
     */
    private String scheduleRef;

    private String notes;
}
