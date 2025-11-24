package com.example.customerservice.dto.request;

import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.entity.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCoreCustomerRequest {
    private String customerName;
    private String username;
    private String nationalId;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
}
