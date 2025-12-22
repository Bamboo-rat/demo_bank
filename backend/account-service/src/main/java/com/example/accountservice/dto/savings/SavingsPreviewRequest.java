package com.example.accountservice.dto.savings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request để xem trước thông tin tiết kiệm trước khi mở sổ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsPreviewRequest {

    @NotNull(message = "Số tiền gửi không được để trống")
    @DecimalMin(value = "100000", message = "Số tiền gửi tối thiểu là 100,000 VND")
    private BigDecimal principalAmount;

    @NotBlank(message = "Kỳ hạn không được để trống")
    private String tenor;

    @NotBlank(message = "Phương thức trả lãi không được để trống")
    private String interestPaymentMethod;
}
