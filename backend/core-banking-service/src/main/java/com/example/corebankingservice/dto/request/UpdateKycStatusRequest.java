package com.example.corebankingservice.dto.request;

import com.example.corebankingservice.entity.enums.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateKycStatusRequest {

    @NotNull(message = "KYC status is required")
    private KycStatus kycStatus;

    @NotBlank(message = "Authorized by is required")
    private String authorizedBy;

    private String note;
}
