package com.example.commonapi.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request payload for account metadata synchronization over Dubbo.
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
    private String accountType;
    private String currency;
    private String status;
    private BigDecimal balance;
    private LocalDateTime openedAt;
}
