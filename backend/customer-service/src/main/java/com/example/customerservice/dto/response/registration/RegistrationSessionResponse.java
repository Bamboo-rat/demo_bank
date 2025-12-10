package com.example.customerservice.dto.response.registration;

import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.redis.model.RegistrationSessionStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistrationSessionResponse {
    String sessionId;
    String phoneNumber;
    RegistrationSessionStatus status;
    Instant expiresAt;
    KycStatus kycStatus;
}
