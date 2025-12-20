package com.example.transactionservice.dubbo.consumer;

import com.example.commonapi.dto.digitalotp.DigitalOtpEnrollmentRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationResponse;
import com.example.commonapi.dubbo.DigitalOtpDubboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DigitalOtpServiceClient {

    @DubboReference(
        version = "1.0.0",
        group = "banking-services",
        timeout = 5000,
        retries = 2,
        check = false
    )
    private DigitalOtpDubboService digitalOtpDubboService;

    /**
     * Validate Digital OTP token
     * @param request validation request
     * @return validation response
     */
    public DigitalOtpValidationResponse validateDigitalOtp(DigitalOtpValidationRequest request) {
        log.info("Validating Digital OTP for customer: {}, transaction: {}", 
            request.getCustomerId(), request.getTransactionId());
        return digitalOtpDubboService.validateDigitalOtp(request);
    }

    /**
     * Enroll customer for Digital OTP
     * @param request enrollment request
     * @return true if enrollment successful
     */
    public boolean enrollDigitalOtp(DigitalOtpEnrollmentRequest request) {
        log.info("Enrolling Digital OTP for customer: {}", request.getCustomerId());
        return digitalOtpDubboService.enrollDigitalOtp(request);
    }

    /**
     * Check Digital OTP enrollment status
     * @param customerId customer ID
     * @return status response
     */
    public DigitalOtpStatusResponse getDigitalOtpStatus(String customerId) {
        return digitalOtpDubboService.getDigitalOtpStatus(customerId);
    }

    /**
     * Unlock Digital OTP for customer (admin operation)
     * @param customerId customer ID
     * @return true if unlock successful
     */
    public boolean unlockDigitalOtp(String customerId) {
        log.info("Unlocking Digital OTP for customer: {}", customerId);
        return digitalOtpDubboService.unlockDigitalOtp(customerId);
    }
}
