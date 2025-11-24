package com.example.customerservice.dto.response;

import com.example.customerservice.entity.enums.CustomerStatus;
import com.example.customerservice.entity.enums.Gender;
import com.example.customerservice.entity.enums.KycStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CustomerResponse {
    private String customerId;
    private String coreBankingId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String nationality;
    private String nationalId;
    private AddressResponse permanentAddress;
    private AddressResponse temporaryAddress;
    private KycStatus kycStatus;
    private CustomerStatus status;
}