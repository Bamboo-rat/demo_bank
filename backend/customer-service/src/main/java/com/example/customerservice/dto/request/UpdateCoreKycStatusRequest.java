package com.example.customerservice.dto.request;

import com.example.customerservice.entity.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCoreKycStatusRequest {
    private KycStatus kycStatus;
    private String authorizedBy;
    private String note;
}
