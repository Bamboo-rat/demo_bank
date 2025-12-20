package com.example.customerservice.service;

import com.example.commonapi.dto.digitalotp.DigitalOtpEnrollmentRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationResponse;

/**
 * Service interface for Digital OTP operations
 */
public interface DigitalOtpService {

    /**
     * Enroll customer for Digital OTP
     */
    boolean enrollDigitalOtp(DigitalOtpEnrollmentRequest request);

    /**
     * Validate Digital OTP token with rate limiting and replay protection
     */
    DigitalOtpValidationResponse validateDigitalOtp(DigitalOtpValidationRequest request);

    /**
     * Get Digital OTP enrollment status
     */
    DigitalOtpStatusResponse getDigitalOtpStatus(String customerId);

    /**
     * Unlock Digital OTP (admin operation)
     */
    boolean unlockDigitalOtp(String customerId);

    /**
     * Cleanup expired used tokens (scheduled job)
     */
    void cleanupExpiredTokens();
}
