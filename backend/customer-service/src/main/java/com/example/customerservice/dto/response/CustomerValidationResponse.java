package com.example.customerservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerValidationResponse {
    private String customerId;
    private boolean valid;
    private String message;
    private String customerName;
    private String cifNumber;
    private String status;
}
