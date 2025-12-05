package com.example.customerservice.service.impl;

import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.request.registration.RegistrationCompleteRequest;
import com.example.customerservice.dto.request.registration.RegistrationIdentityRequest;
import com.example.customerservice.dto.request.registration.RegistrationProfileRequest;
import com.example.customerservice.dto.request.registration.RegistrationStartRequest;
import com.example.customerservice.dto.request.registration.RegistrationVerifyRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.dto.response.registration.RegistrationSessionResponse;
import com.example.customerservice.dto.response.registration.RegistrationStartResponse;
import com.example.customerservice.exception.InvalidOtpException;
import com.example.customerservice.exception.OtpExpiredException;
import com.example.customerservice.exception.RegistrationRateLimitException;
import com.example.customerservice.exception.RegistrationSessionDataException;
import com.example.customerservice.exception.RegistrationSessionNotFoundException;
import com.example.customerservice.exception.RegistrationSessionStateException;
import com.example.customerservice.redis.RegistrationSessionRepository;
import com.example.customerservice.redis.model.RegistrationIdentityData;
import com.example.customerservice.redis.model.RegistrationProfileData;
import com.example.customerservice.redis.model.RegistrationSession;
import com.example.customerservice.redis.model.RegistrationSessionStatus;
import com.example.customerservice.service.CustomerService;
import com.example.customerservice.service.RegistrationService;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private static final Duration OTP_TTL = Duration.ofMinutes(3);
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final SecureRandom secureRandom = new SecureRandom();

    private final RegistrationSessionRepository registrationSessionRepository;
    private final CustomerService customerService;

    @Override
    public RegistrationStartResponse start(RegistrationStartRequest request) {
        String phoneNumber = normalizePhone(request.getPhoneNumber());
        log.info("Starting registration for phone {}", phoneNumber);

        if (registrationSessionRepository.getOtpCode(phoneNumber).isPresent()) {
            throw new RegistrationRateLimitException("OTP đã được gửi, vui lòng thử lại sau");
        }

        registrationSessionRepository.deleteSession(phoneNumber);

        String otp = generateOtp();
        registrationSessionRepository.saveOtpCode(phoneNumber, otp, OTP_TTL);
        log.info("Generated registration OTP {} for phone {}", otp, phoneNumber);

        return RegistrationStartResponse.builder()
                .phoneNumber(phoneNumber)
                .otpTtlSeconds(OTP_TTL.toSeconds())
                .build();
    }

    @Override
    public RegistrationSessionResponse verify(RegistrationVerifyRequest request) {
        String phoneNumber = normalizePhone(request.getPhoneNumber());
        log.info("Verifying OTP for phone {}", phoneNumber);
        Optional<String> cachedOtp = registrationSessionRepository.getOtpCode(phoneNumber);
        if (cachedOtp.isEmpty()) {
            throw new OtpExpiredException("OTP đã hết hạn hoặc không tồn tại");
        }

        if (!cachedOtp.get().equals(request.getOtp())) {
            throw new InvalidOtpException("OTP không chính xác");
        }

        registrationSessionRepository.deleteOtpCode(phoneNumber);

        String sessionId = UUID.randomUUID().toString();
        RegistrationSession session = RegistrationSession.builder()
                .sessionId(sessionId)
                .phoneNumber(phoneNumber)
                .status(RegistrationSessionStatus.OTP_VERIFIED)
                .build();
        session.touch();
        registrationSessionRepository.saveSession(session, SESSION_TTL);

        return toSessionResponse(session);
    }

    @Override
    public RegistrationSessionResponse saveProfile(RegistrationProfileRequest request) {
        RegistrationSession session = loadSession(request.getPhoneNumber(), request.getSessionId());
        log.info("Persisting profile data for session {} (phone {})", session.getSessionId(), session.getPhoneNumber());
        ensureState(session, RegistrationSessionStatus.OTP_VERIFIED, RegistrationSessionStatus.PROFILE_IN_PROGRESS);

        RegistrationProfileData profileData = RegistrationProfileData.builder()
                .password(request.getPassword())
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .email(request.getEmail())
                .occupation(request.getOccupation())
                .position(request.getPosition())
                .build();

        session.setProfileData(profileData);
        session.setStatus(RegistrationSessionStatus.PROFILE_IN_PROGRESS);
        session.touch();
        registrationSessionRepository.saveSession(session, SESSION_TTL);
        return toSessionResponse(session);
    }

    @Override
    public RegistrationSessionResponse saveIdentity(RegistrationIdentityRequest request) {
        RegistrationSession session = loadSession(request.getPhoneNumber(), request.getSessionId());
        log.info("Persisting identity data for session {} (phone {})", session.getSessionId(), session.getPhoneNumber());
        ensureState(session, RegistrationSessionStatus.PROFILE_IN_PROGRESS);

        RegistrationIdentityData identityData = RegistrationIdentityData.builder()
                .nationalId(request.getNationalId())
                .issueDateNationalId(request.getIssueDateNationalId())
                .placeOfIssueNationalId(request.getPlaceOfIssueNationalId())
                .permanentAddress(request.getPermanentAddress())
                .temporaryAddress(request.getTemporaryAddress())
                .documentFrontImage(request.getDocumentFrontImage())
                .documentBackImage(request.getDocumentBackImage())
                .selfieImage(request.getSelfieImage())
                .build();

        session.setIdentityData(identityData);
        session.setStatus(RegistrationSessionStatus.READY_FOR_CREATION);
        session.touch();
        registrationSessionRepository.saveSession(session, SESSION_TTL);
        return toSessionResponse(session);
    }

    @Override
    public CustomerResponse complete(RegistrationCompleteRequest request) {
        RegistrationSession session = loadSession(request.getPhoneNumber(), request.getSessionId());
        log.info("Completing registration for session {} (phone {})", session.getSessionId(), session.getPhoneNumber());
        ensureState(session, RegistrationSessionStatus.READY_FOR_CREATION);

        if (session.getProfileData() == null) {
            throw new RegistrationSessionDataException("profileData");
        }
        if (session.getIdentityData() == null) {
            throw new RegistrationSessionDataException("identityData");
        }

        CustomerRegisterRequest registerRequest = buildRegisterRequest(session);
        CustomerResponse response = customerService.registerCustomer(registerRequest);

        session.setStatus(RegistrationSessionStatus.COMPLETED);
        registrationSessionRepository.deleteSession(session.getPhoneNumber());
        return response;
    }

    private RegistrationSession loadSession(String phoneNumber, String sessionId) {
        String normalizedPhone = normalizePhone(phoneNumber);
        RegistrationSession session = registrationSessionRepository.findSession(normalizedPhone)
                .orElseThrow(() -> new RegistrationSessionNotFoundException(normalizedPhone, sessionId));

        if (!sessionId.equals(session.getSessionId())) {
            throw new RegistrationSessionNotFoundException(normalizedPhone, sessionId);
        }
        return session;
    }

    private void ensureState(RegistrationSession session, RegistrationSessionStatus... expectedStatuses) {
        for (RegistrationSessionStatus expectedStatus : expectedStatuses) {
            if (session.getStatus() == expectedStatus) {
                return;
            }
        }
        throw new RegistrationSessionStateException(
                expectedDescriptions(expectedStatuses),
                session.getStatus() != null ? session.getStatus().name() : "null"
        );
    }

    private CustomerRegisterRequest buildRegisterRequest(RegistrationSession session) {
        RegistrationProfileData profile = session.getProfileData();
        RegistrationIdentityData identity = session.getIdentityData();

        return CustomerRegisterRequest.builder()
                .password(profile.getPassword())
                .fullName(profile.getFullName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .nationality(profile.getNationality())
                .nationalId(identity.getNationalId())
                .issueDateNationalId(identity.getIssueDateNationalId())
                .placeOfIssueNationalId(identity.getPlaceOfIssueNationalId())
                .occupation(profile.getOccupation())
                .position(profile.getPosition())
                .email(profile.getEmail())
                .phoneNumber(session.getPhoneNumber())
                .permanentAddress(identity.getPermanentAddress())
                .temporaryAddress(identity.getTemporaryAddress())
                .build();
    }

    private RegistrationSessionResponse toSessionResponse(RegistrationSession session) {
        Instant expiryReference = session.getUpdatedAt() != null
                ? session.getUpdatedAt()
                : Optional.ofNullable(session.getCreatedAt()).orElse(Instant.now());

        return RegistrationSessionResponse.builder()
                .sessionId(session.getSessionId())
                .phoneNumber(session.getPhoneNumber())
                .status(session.getStatus())
                .expiresAt(expiryReference.plus(SESSION_TTL))
                .build();
    }

    private String generateOtp() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format(Locale.US, "%06d", value);
    }

    private String expectedDescriptions(RegistrationSessionStatus... statuses) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            builder.append(statuses[i].name());
            if (i < statuses.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private String normalizePhone(String phoneNumber) {
        return phoneNumber != null ? phoneNumber.replaceAll("[\\s./]", "") : null;
    }
}
