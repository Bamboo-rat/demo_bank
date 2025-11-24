package com.example.customerservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EkycResponse {
    private String status; // SUCCESS, FAILED
    private String message;
    private String fullName;
    private LocalDate dateOfBirth;
    private String nationalId;
    private Boolean isVerified;
}