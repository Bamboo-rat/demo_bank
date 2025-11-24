package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.CustomerStatus;
import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CifResponse {
    private String cifId;
    private String cifNumber;
    private String customerName;
    private String username;
    private CustomerStatus customerStatus;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
    private LocalDateTime createdDate;
}
