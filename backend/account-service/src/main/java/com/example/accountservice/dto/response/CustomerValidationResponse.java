package com.example.accountservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerValidationResponse {
    private String customerId;
    private boolean valid;
    private String message;
    private String customerName;
    private String cifNumber;
    private String status;
}
