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
public class EarlySettlementRequest {
    
    @NotBlank(message = "Loan account ID is required")
    private String loanAccountId;
    
    private String paymentMethod;
    private String notes;
}
