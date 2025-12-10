package com.example.customerservice.dto.response;

import com.example.customerservice.entity.enums.KycStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreCifResponse {
    private String cifNumber;
    private String customerName;
    private KycStatus kycStatus;
}
