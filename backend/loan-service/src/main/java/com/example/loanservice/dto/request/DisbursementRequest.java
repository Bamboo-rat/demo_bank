package com.example.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementRequest {
    
    @NotBlank(message = "Loan account ID is required")
    private String loanAccountId;
}
