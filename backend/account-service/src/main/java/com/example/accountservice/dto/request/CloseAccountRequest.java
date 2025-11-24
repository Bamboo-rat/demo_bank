package com.example.accountservice.dto.request;

import com.example.accountservice.entity.enums.ClosureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseAccountRequest {

    @NotBlank(message = "validation.required")
    @Size(min = 10, max = 20, message = "validation.invalid")
    private String accountNumber;

    @NotBlank(message = "validation.required")
    private String customerId;

    @Size(max = 500, message = "validation.max.length")
    private String reason;

    private ClosureType closureType;
}
