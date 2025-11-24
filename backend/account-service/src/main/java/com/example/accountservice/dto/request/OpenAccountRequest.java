package com.example.accountservice.dto.request;

import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAccountRequest {
    
    @NotBlank(message = "validation.required")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "validation.invalid"
    )
    private String customerId;

    @NotNull(message = "validation.required")
    private AccountType accountType;

    @NotNull(message = "validation.required")
    private Currency currency;

}
