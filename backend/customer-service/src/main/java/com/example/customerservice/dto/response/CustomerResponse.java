package com.example.customerservice.dto.response;

import com.example.customerservice.entity.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private String customerId;
    private String authProviderId;
    private String cifNumber;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean emailVerified;
    
    // Basic profile information
    private LocalDate dateOfBirth;
    private Gender gender;
    private AddressResponse permanentAddress;
    private AddressResponse temporaryAddress;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Note: Sensitive KYC data (nationalId, kyc_status, risk_level, nationality, occupation, etc.)
    // should be fetched from Core Banking Service via cifNumber
}