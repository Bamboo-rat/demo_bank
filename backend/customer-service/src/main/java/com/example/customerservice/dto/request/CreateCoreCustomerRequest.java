package com.example.customerservice.dto.request;

import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.entity.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CreateCoreCustomerRequest {
    private String customerName;
    private String username;
    private String nationalId;
    private String nationality;
    private LocalDate issueDateNationalId;
    private String placeOfIssueNationalId;
    private String occupation;
    private String position;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
}
