package com.example.loanservice.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request duyệt khoản vay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequest {

    @NotNull(message = "Số tiền duyệt không được để trống")
    @DecimalMin(value = "10000000", message = "Số tiền duyệt tối thiểu là 10,000,000 VND")
    private BigDecimal approvedAmount;

    @NotNull(message = "Lãi suất không được để trống")
    @DecimalMin(value = "5.0", message = "Lãi suất tối thiểu là 5%")
    @DecimalMax(value = "25.0", message = "Lãi suất tối đa là 25%")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.5", message = "Lãi suất phạt tối thiểu là 0.5%")
    private BigDecimal penaltyRate;

    @NotBlank(message = "Tài khoản giải ngân không được để trống")
    private String disbursementAccount;

    @NotBlank(message = "Tài khoản trả nợ không được để trống")
    private String repaymentAccount;

    /**
     * Người duyệt (optional)
     */
    private String approvedBy;

    private String notes;
}
