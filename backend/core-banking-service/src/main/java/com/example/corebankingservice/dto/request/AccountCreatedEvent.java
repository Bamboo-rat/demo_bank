package com.example.corebankingservice.dto.request;

import com.example.corebankingservice.entity.enums.AccountType;
import com.example.corebankingservice.entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountCreatedEvent {
    private String cifNumber;
    private String accountNumber;
    private AccountType accountType;
    private Currency currency;
}
