package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.CustomerStatus;
import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CifStatusResponse {
    private String cifNumber;
    private String customerName;
    private CustomerStatus status;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
    private Boolean canTransact;
}
