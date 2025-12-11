package com.example.commonapi.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for customer basic info via Dubbo
 * Contains minimal information needed for display purposes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBasicInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String customerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private String customerStatus;
    private String cifNumber;
}
