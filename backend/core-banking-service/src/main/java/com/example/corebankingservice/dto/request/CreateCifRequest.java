package com.example.corebankingservice.dto.request;

import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CreateCifRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    private String nationality;

    private LocalDate issueDateNationalId;

    private String placeOfIssueNationalId;

    private String occupation;

    private String position;

    @NotNull(message = "KYC status is required")
    private KycStatus kycStatus;

    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel;
}
