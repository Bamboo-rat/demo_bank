package com.example.corebankingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCifStatusRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "ACTIVE|SUSPENDED|BLOCKED|CLOSED",
            message = "Action must be ACTIVE, SUSPENDED, BLOCKED or CLOSED")
    private String action;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Authorized by is required")
    private String authorizedBy;
}
