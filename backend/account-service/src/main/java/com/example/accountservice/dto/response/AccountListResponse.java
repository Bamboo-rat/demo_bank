package com.example.accountservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for account list response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa danh sách tài khoản của khách hàng")
public class AccountListResponse {

    @Schema(description = "ID khách hàng", example = "CUST-2024-0001")
    private String customerId;

    @Schema(description = "Danh sách tài khoản")
    private List<AccountResponse> accounts;

    @Schema(description = "Tổng số tài khoản", example = "3")
    private int totalCount;

    @Schema(description = "Timestamp (epoch milliseconds)", example = "1704096000000")
    private long timestamp;
}
