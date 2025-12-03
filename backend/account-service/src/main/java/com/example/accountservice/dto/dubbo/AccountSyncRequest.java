package com.example.accountservice.dto.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for Dubbo account sync
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSyncRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountNumber;
    private String customerId;
    private String cifNumber;
    private String accountType; // CHECKING, SAVINGS, CREDIT
    private String currency;    // VND, USD, EUR
    private String status;      // ACTIVE, DORMANT, CLOSED, FROZEN, BLOCKED
    private BigDecimal balance;
    private LocalDateTime openedAt;
}
