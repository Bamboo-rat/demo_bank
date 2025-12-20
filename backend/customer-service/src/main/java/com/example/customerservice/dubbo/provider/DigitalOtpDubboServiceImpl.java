package com.example.customerservice.dubbo.provider;

import com.example.commonapi.dto.digitalotp.DigitalOtpEnrollmentRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationResponse;
import com.example.commonapi.dubbo.DigitalOtpDubboService;
import com.example.customerservice.service.DigitalOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Dubbo provider implementation for Digital OTP operations
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "banking-services",
    timeout = 5000
)
@RequiredArgsConstructor
public class DigitalOtpDubboServiceImpl implements DigitalOtpDubboService {

    private final DigitalOtpService digitalOtpService;

    @Override
    public boolean enrollDigitalOtp(DigitalOtpEnrollmentRequest request) {
        log.info("Dubbo: Enrolling Digital OTP for customer: {}", request.getCustomerId());
        return digitalOtpService.enrollDigitalOtp(request);
    }

    @Override
    public DigitalOtpValidationResponse validateDigitalOtp(DigitalOtpValidationRequest request) {
        log.info("Dubbo: Validating Digital OTP for customer: {}, transaction: {}", 
            request.getCustomerId(), request.getTransactionId());
        return digitalOtpService.validateDigitalOtp(request);
    }

    @Override
    public DigitalOtpStatusResponse getDigitalOtpStatus(String customerId) {
        log.info("Dubbo: Getting Digital OTP status for customer: {}", customerId);
        return digitalOtpService.getDigitalOtpStatus(customerId);
    }

    @Override
    public boolean unlockDigitalOtp(String customerId) {
        log.info("Dubbo: Unlocking Digital OTP for customer: {}", customerId);
        return digitalOtpService.unlockDigitalOtp(customerId);
    }
}
