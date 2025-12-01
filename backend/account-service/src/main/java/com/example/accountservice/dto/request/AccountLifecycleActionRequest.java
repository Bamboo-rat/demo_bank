package com.example.accountservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLifecycleActionRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Performed by is required")
    private String performedBy;
}
