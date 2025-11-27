package com.example.corebankingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountLifecycleActionRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "PerformedBy is required")
    private String performedBy;
}
