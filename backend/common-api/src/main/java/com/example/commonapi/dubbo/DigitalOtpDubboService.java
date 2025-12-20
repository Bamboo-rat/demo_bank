package com.example.commonapi.dubbo;

import com.example.commonapi.dto.digitalotp.DigitalOtpEnrollmentRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationResponse;

/**
 * Dubbo service interface for Digital OTP operations
 * Provides enrollment, validation, and status check
 */
public interface DigitalOtpDubboService {

    /**
     * Enroll customer for Digital OTP
     * Stores public key and PIN hash
     * 
     * @param request enrollment details
     * @return true if enrollment successful
     */
    boolean enrollDigitalOtp(DigitalOtpEnrollmentRequest request);

    /**
     * Validate Digital OTP token
     * Verifies signature, checks PIN, enforces rate limit and replay protection
     * 
     * @param request validation request with token and transaction payload
     * @return validation response with result and remaining attempts
     */
    DigitalOtpValidationResponse validateDigitalOtp(DigitalOtpValidationRequest request);

    /**
     * Check Digital OTP enrollment status
     * 
     * @param customerId customer ID
     * @return enrollment and lock status
     */
    DigitalOtpStatusResponse getDigitalOtpStatus(String customerId);

    /**
     * Unlock Digital OTP for customer (admin operation)
     * 
     * @param customerId customer ID
     * @return true if unlock successful
     */
    boolean unlockDigitalOtp(String customerId);
}
