package com.example.transactionservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpEnrollmentRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.transactionservice.dubbo.consumer.DigitalOtpServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Digital OTP Controller
 * Handles enrollment, updates, status checks, and admin unlock operations
 */
@Slf4j
@RestController
@RequestMapping("/api/digital-otp")
@RequiredArgsConstructor
public class DigitalOtpController {

    private final DigitalOtpServiceClient digitalOtpServiceClient;

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<Boolean>> enrollDigitalOtp(
            @Valid @RequestBody DigitalOtpEnrollmentRequest request) {

        log.info("Digital OTP enrollment request for customer: {}", request.getCustomerId());

        try {
            boolean result = digitalOtpServiceClient.enrollDigitalOtp(request);
            if (result) {
                return ResponseEntity.ok(
                        ApiResponse.success("Digital OTP enrolled successfully.", true));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Digital OTP enrollment failed. Please try again."));
        } catch (Exception e) {
            log.error("Error during Digital OTP enrollment for customer: {}",
                    request.getCustomerId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during enrollment: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Boolean>> updateDigitalOtp(
            @Valid @RequestBody DigitalOtpEnrollmentRequest request) {

        log.info("Digital OTP update request for customer: {}", request.getCustomerId());

        try {
            boolean result = digitalOtpServiceClient.enrollDigitalOtp(request);
            if (result) {
                return ResponseEntity.ok(
                        ApiResponse.success("Digital OTP updated successfully.", true));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Digital OTP update failed. Please try again."));
        } catch (Exception e) {
            log.error("Error during Digital OTP update for customer: {}",
                    request.getCustomerId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during update: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{customerId}")
    public ResponseEntity<ApiResponse<DigitalOtpStatusResponse>> getDigitalOtpStatus(
            @PathVariable String customerId) {

        log.info("Digital OTP status check for customer: {}", customerId);

        try {
                DigitalOtpStatusResponse status = digitalOtpServiceClient.getDigitalOtpStatus(customerId);
                return ResponseEntity.ok(
                    ApiResponse.success("Digital OTP status retrieved successfully.", status));
        } catch (Exception e) {
            log.error("Error checking Digital OTP status for customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while checking status: " + e.getMessage()));
        }
    }

    @PostMapping("/unlock/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> unlockDigitalOtp(@PathVariable String customerId) {

        log.info("Digital OTP unlock request for customer: {} by admin", customerId);

        try {
            boolean result = digitalOtpServiceClient.unlockDigitalOtp(customerId);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Digital OTP unlocked successfully for customer: " + customerId, true));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Digital OTP unlock failed. Customer may not be enrolled."));
        } catch (Exception e) {
            log.error("Error unlocking Digital OTP for customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during unlock: " + e.getMessage()));
        }
    }
}
