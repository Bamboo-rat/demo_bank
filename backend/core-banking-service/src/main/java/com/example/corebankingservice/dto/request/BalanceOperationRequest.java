package com.example.corebankingservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request for balance operations (Debit/Credit)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request cho các thao tác số dư (Trừ tiền/Cộng tiền)")
public class BalanceOperationRequest {

    @Schema(
            description = "Số tài khoản cần thực hiện thao tác",
            example = "37029832305730",
            required = true,
            maxLength = 20
    )
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @Schema(
            description = "Số tiền cần thao tác (VND). Phải lớn hơn 0",
            example = "500000.00",
            required = true,
            minimum = "0.01"
    )
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(
            description = "Mã tham chiếu giao dịch duy nhất (UUID). Dùng cho idempotency - cùng transactionReference chỉ xử lý 1 lần",
            example = "TX-20241214-UUID-001",
            required = true
    )
    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @Schema(
            description = "Mô tả chi tiết giao dịch",
            example = "Chuyển khoản cho Nguyễn Văn A"
    )
    private String description;

    @Schema(
            description = "Người thực hiện thao tác (username hoặc system)",
            example = "system"
    )
    private String performedBy;
}
