package com.example.loanservice.dto.request;

import com.example.loanservice.entity.enums.LoanPurpose;
import com.example.loanservice.entity.enums.RepaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request đăng ký vay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "Số tiền vay không được để trống")
    @DecimalMin(value = "10000000", message = "Số tiền vay tối thiểu là 10,000,000 VND")
    @DecimalMax(value = "1000000000", message = "Số tiền vay tối đa là 1,000,000,000 VND")
    private BigDecimal requestedAmount;

    @NotNull(message = "Kỳ hạn không được để trống")
    @Min(value = 6, message = "Kỳ hạn tối thiểu là 6 tháng")
    @Max(value = 240, message = "Kỳ hạn tối đa là 240 tháng")
    private Integer tenor;

    @NotNull(message = "Mục đích vay không được để trống")
    private LoanPurpose purpose;

    @NotNull(message = "Phương thức trả nợ không được để trống")
    private RepaymentMethod repaymentMethod;

    @DecimalMin(value = "0", message = "Thu nhập hàng tháng phải >= 0")
    private BigDecimal monthlyIncome;

    private String employmentStatus;

    private String collateralInfo;

    private String notes;
}
