package com.example.accountservice.dto.response;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin tài khoản ngân hàng")
public class AccountResponse {

    @Schema(description = "ID tài khoản", example = "ACC-2024-0001")
    private String accountId;
    
    @Schema(description = "Số tài khoản", example = "ACC-2024-0001")
    private String accountNumber;
    
    @Schema(description = "ID khách hàng", example = "CUST-2024-0001")
    private String customerId;
    
    @Schema(description = "Loại tài khoản", example = "SAVINGS", allowableValues = {"CHECKING", "SAVINGS", "CREDIT"})
    private AccountType accountType;
    
    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "FROZEN", "CLOSED"})
    private AccountStatus status;
    
    @Schema(description = "Loại tiền tệ", example = "VND", allowableValues = {"VND", "USD", "EUR"})
    private Currency currency;
    
    @Schema(description = "Ngày mở tài khoản", example = "2024-01-01T09:00:00")
    private LocalDateTime openedDate;
    
    @Schema(description = "Ngày đóng tài khoản (nếu đã đóng)", example = "2024-12-31T17:00:00")
    private LocalDateTime closedDate;
    
    @Schema(description = "Ngày tạo bản ghi", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Ngày cập nhật bản ghi", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    // Customer info (from customer-service via Dubbo)
    @Schema(description = "Tên khách hàng (từ customer-service)", example = "Nguyễn Văn A")
    private String customerName;
    
    @Schema(description = "Trạng thái khách hàng (từ customer-service)", example = "ACTIVE")
    private String customerStatus;
    
    // Core banking reference
    @Schema(description = "Số CIF (tham chiếu đến core-banking)", example = "CIF-2024-0001")
    private String cifNumber; // Reference to CIF in core-banking
    
    // Balance info (from core-banking-service via Feign)
    @Schema(description = "Số dư thực tế (từ core-banking-service)", example = "10000000.00")
    private BigDecimal balance;
    
    @Schema(description = "Số dư khả dụng (từ core-banking-service)", example = "9500000.00")
    private BigDecimal availableBalance;
}
