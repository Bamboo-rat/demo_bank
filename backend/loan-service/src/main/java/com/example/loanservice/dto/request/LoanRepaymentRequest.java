package com.example.loanservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request trả nợ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentRequest {

    @NotNull(message = "ID kỳ trả nợ không được để trống")
    private String scheduleId;

    @NotNull(message = "Số tiền trả không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền trả phải > 0")
    private BigDecimal amount;

    private String paymentMethod;

    private String notes;
}
