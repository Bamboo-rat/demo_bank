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
    private LocalDate dateOfBirth;
    private Gender gender;
    private AddressResponse permanentAddress;
    private AddressResponse temporaryAddress;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}