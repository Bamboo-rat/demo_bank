package com.example.customerservice.dto.response.registration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistrationStartResponse {
    String phoneNumber;
    long otpTtlSeconds;
}
