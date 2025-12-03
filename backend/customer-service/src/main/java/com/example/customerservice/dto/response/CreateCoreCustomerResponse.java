package com.example.customerservice.dto.response;

import lombok.Data;

@Data
public class CreateCoreCustomerResponse {
    private String cifId;
    private String cifNumber;
    private String customerName;
    private String username;
    private String accountNumber;
    public String getCoreCustomerId() {
        return cifId;
    }
}
