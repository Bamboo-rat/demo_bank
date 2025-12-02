package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.CustomerStatus;
import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CifResponse {
    private String cifId;
    private String cifNumber;
    private String customerName;
    private String username;
    private String nationalId;
    private String nationality;
    private LocalDate issueDateNationalId;
    private String placeOfIssueNationalId;
    private String occupation;
    private String position;
    private CustomerStatus customerStatus;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
    private String accountNumber; 
    private LocalDateTime lastTransactionDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
