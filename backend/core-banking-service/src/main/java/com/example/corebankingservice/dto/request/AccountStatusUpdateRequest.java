package com.example.corebankingservice.dto.request;

import com.example.corebankingservice.entity.enums.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountStatusUpdateRequest {

    @NotNull(message = "Target status is required")
    private AccountStatus status;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "PerformedBy is required")
    private String performedBy;
}
