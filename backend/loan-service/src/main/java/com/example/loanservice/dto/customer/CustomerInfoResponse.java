package com.example.loanservice.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfoResponse implements Serializable {
    private String customerId;
    private String authProviderId;
    private String cifId;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private boolean kycCompleted;
    private String accountId;
}
