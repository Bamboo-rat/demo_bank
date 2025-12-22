package com.example.accountservice.dto.corebank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO sau khi lock tiền thành công từ Core Banking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockFundsResponse {
    
    private String lockId;
    private String accountNumber;
    private BigDecimal lockedAmount;
    private BigDecimal availableBalance;
    private String lockType;
    private String referenceId;
    private LocalDateTime lockedAt;
    private String status;
}
