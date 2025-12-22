package com.example.corebankingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO để lock tiền trong tài khoản
 * Dùng cho các giao dịch cần hold tiền tạm thời (tiết kiệm, thế chấp, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockFundsRequest {
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Lock type is required")
    private String lockType; // SAVINGS, COLLATERAL, HOLD, etc.
    
    @NotBlank(message = "Reference ID is required")
    private String referenceId; // ID của transaction/savings account liên quan
    
    private String description;
}
