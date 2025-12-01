package com.example.accountservice.dto.response;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountDetailResponse {

    private String accountId;
    private String accountNumber;
    private String cifNumber;
    private AccountType accountType;
    private AccountStatus status;
    private Currency currency;
    private BigDecimal balance;
    private BigDecimal holdAmount;
    private boolean amlFlag;
    private LocalDateTime openedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
