package com.example.customerservice.service;

import com.example.customerservice.dto.request.registration.RegistrationCompleteRequest;
import com.example.customerservice.dto.request.registration.RegistrationIdentityRequest;
import com.example.customerservice.dto.request.registration.RegistrationProfileRequest;
import com.example.customerservice.dto.request.registration.RegistrationStartRequest;
import com.example.customerservice.dto.request.registration.RegistrationVerifyRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.dto.response.registration.RegistrationSessionResponse;
import com.example.customerservice.dto.response.registration.RegistrationStartResponse;
import com.example.customerservice.entity.enums.KycStatus;

public interface RegistrationService {

    RegistrationStartResponse start(RegistrationStartRequest request);

    RegistrationSessionResponse verify(RegistrationVerifyRequest request);

    RegistrationSessionResponse saveProfile(RegistrationProfileRequest request);

    RegistrationSessionResponse saveIdentity(RegistrationIdentityRequest request);

    CustomerResponse complete(RegistrationCompleteRequest request);

    void markKycStatus(String phoneNumber, KycStatus kycStatus);
}
