package com.example.accountservice.dto.request;

import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAccountCoreRequest {

    @NotBlank(message = "CIF number is required")
    private String cifNumber;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Currency is required")
    private Currency currency;

    private String createdBy;

    private String description;
}
