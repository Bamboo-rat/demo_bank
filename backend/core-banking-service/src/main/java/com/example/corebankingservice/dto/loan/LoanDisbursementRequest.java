package com.example.corebankingservice.dto.loan;

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
 * Request giải ngân khoản vay
 * Gọi từ Loan Service sau khi duyệt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDisbursementRequest {

    /**
     * Reference đến Loan Service (loan_id từ Loan Service)
     */
    @NotBlank(message = "Loan service reference không được để trống")
    private String loanServiceRef;

    @NotBlank(message = "CIF ID không được để trống")
    private String cifId;

    @NotBlank(message = "Account ID không được để trống")
    private String accountId;

    @NotNull(message = "Số tiền giải ngân không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền giải ngân phải > 0")
    private BigDecimal disbursementAmount;

    @NotNull(message = "Lãi suất không được để trống")
    private BigDecimal interestRate;

    @NotNull(message = "Kỳ hạn không được để trống")
    private Integer termMonths;

    @NotNull(message = "Ngày giải ngân không được để trống")
    private LocalDate disbursementDate;

    @NotNull(message = "Ngày đáo hạn không được để trống")
    private LocalDate maturityDate;

    private String notes;
}
