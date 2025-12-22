package com.example.corebankingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO sau khi lock tiền thành công
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockFundsResponse {
    
    private String lockId; // ID của bản ghi lock
    private String accountNumber;
    private BigDecimal lockedAmount;
    private BigDecimal availableBalance; // Số dư khả dụng sau khi lock
    private String lockType;
    private String referenceId;
    private LocalDateTime lockedAt;
    private String status; // LOCKED, RELEASED
}
