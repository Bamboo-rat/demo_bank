package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for balance operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response cho các thao tác số dư")
public class BalanceOperationResponse {

    @Schema(description = "Số tài khoản", example = "37029832305730")
    private String accountNumber;

    @Schema(description = "Số dư trước khi thực hiện thao tác", example = "10000000.00")
    private BigDecimal previousBalance;

    @Schema(description = "Số tiền của thao tác (số tiền trừ/cộng)", example = "500000.00")
    private BigDecimal operationAmount;

    @Schema(description = "Số dư mới sau khi thực hiện thao tác", example = "9500000.00")
    private BigDecimal newBalance;

    @Schema(description = "Số dư khả dụng (số dư thực tế - số tiền giữ)", example = "9500000.00")
    private BigDecimal availableBalance;

    @Schema(description = "Đơn vị tiền tệ", example = "VND")
    private Currency currency;

    @Schema(description = "Mã tham chiếu giao dịch", example = "TX-20241214-UUID-001")
    private String transactionReference;

    @Schema(description = "Loại thao tác", example = "DEBIT", allowableValues = {"DEBIT", "CREDIT", "HOLD", "RELEASE_HOLD"})
    private String operationType;

    @Schema(description = "Thời gian thực hiện thao tác", example = "2024-12-14T10:30:00")
    private LocalDateTime operationTime;

    @Schema(description = "Thông báo kết quả", example = "Debit operation completed successfully")
    private String message;
}
