package com.example.customerservice.service.impl;

import com.example.commonapi.dto.digitalotp.DigitalOtpEnrollmentRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationResponse;
import com.example.customerservice.entity.Customer;
import com.example.customerservice.entity.DigitalOtpUsedToken;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.repository.DigitalOtpUsedTokenRepository;
import com.example.customerservice.service.DigitalOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalOtpServiceImpl implements DigitalOtpService {

    private final CustomerRepository customerRepository;
    private final DigitalOtpUsedTokenRepository usedTokenRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_HOURS = 24;
    private static final long TOKEN_VALIDITY_SECONDS = 30;
    private static final int TOKEN_CLEANUP_DAYS = 7;

    @Override
    @Transactional
    public boolean enrollDigitalOtp(DigitalOtpEnrollmentRequest request) {
        log.info("Enrolling Digital OTP for customer: {}", request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        // Store public key and PIN hash
        customer.setDigitalPublicKey(request.getDigitalPublicKey());
        customer.setDigitalPinHash(request.getDigitalPinHash());
        customer.setDigitalOtpEnabled(true);
        customer.setDigitalOtpFailedAttempts(0);
        customer.setDigitalOtpLockedUntil(null);
        customer.setDigitalOtpEnrolledAt(LocalDateTime.now());

        customerRepository.save(customer);
        log.info("Digital OTP enrollment successful for customer: {}", request.getCustomerId());
        return true;
    }

    @Override
    @Transactional
    public DigitalOtpValidationResponse validateDigitalOtp(DigitalOtpValidationRequest request) {
        log.info("Validating Digital OTP for customer: {}, transaction: {}", 
            request.getCustomerId(), request.getTransactionId());

        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        // Check if Digital OTP is enabled
        if (!customer.isDigitalOtpEnabled()) {
            return buildErrorResponse("ERROR_NOT_ENROLLED", 
                "Digital OTP not enrolled for this customer", 0, null);
        }

        // Check if account is locked
        if (customer.getDigitalOtpLockedUntil() != null && 
            customer.getDigitalOtpLockedUntil().isAfter(LocalDateTime.now())) {
            long lockedUntil = customer.getDigitalOtpLockedUntil()
                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            return buildErrorResponse("ERROR_LOCKED", 
                "Digital OTP is locked due to too many failed attempts", 
                0, lockedUntil);
        }

        // Clear lock if expired
        if (customer.getDigitalOtpLockedUntil() != null && 
            customer.getDigitalOtpLockedUntil().isBefore(LocalDateTime.now())) {
            customer.setDigitalOtpLockedUntil(null);
            customer.setDigitalOtpFailedAttempts(0);
        }

        // Check for replay attack
        if (usedTokenRepository.existsByTransactionId(request.getTransactionId())) {
            log.warn("Replay attack detected for transaction: {}", request.getTransactionId());
            incrementFailedAttempts(customer);
            return buildErrorResponse("ERROR_REPLAY", 
                "This transaction has already been validated", 
                MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
        }

        // Validate timestamp (30s window)
        long currentTimestamp = Instant.now().toEpochMilli();
        if (Math.abs(currentTimestamp - request.getTimestamp()) > TOKEN_VALIDITY_SECONDS * 1000) {
            log.warn("Token expired. Current: {}, Request: {}", currentTimestamp, request.getTimestamp());
            incrementFailedAttempts(customer);
            return buildErrorResponse("ERROR_EXPIRED", 
                "Digital OTP token has expired", 
                MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
        }

        // Verify signature
        try {
            boolean signatureValid = verifySignature(
                customer.getDigitalPublicKey(),
                buildPayload(request),
                request.getDigitalOtpToken()
            );

            if (!signatureValid) {
                log.warn("Invalid signature for transaction: {}", request.getTransactionId());
                incrementFailedAttempts(customer);
                return buildErrorResponse("ERROR_INVALID_SIGNATURE", 
                    "Invalid Digital OTP signature", 
                    MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
            }

            // Success - reset failed attempts and record token usage
            customer.setDigitalOtpFailedAttempts(0);
            customerRepository.save(customer);

            recordTokenUsage(request);

            log.info("Digital OTP validation successful for transaction: {}", request.getTransactionId());
            return DigitalOtpValidationResponse.builder()
                .valid(true)
                .message("Digital OTP validation successful")
                .remainingAttempts(MAX_FAILED_ATTEMPTS)
                .build();

        } catch (Exception e) {
            log.error("Error validating Digital OTP signature", e);
            incrementFailedAttempts(customer);
            return buildErrorResponse("ERROR_VALIDATION_FAILED", 
                "Digital OTP validation failed: " + e.getMessage(), 
                MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
        }
    }

    @Override
    public DigitalOtpStatusResponse getDigitalOtpStatus(String customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        boolean locked = customer.getDigitalOtpLockedUntil() != null && 
            customer.getDigitalOtpLockedUntil().isAfter(LocalDateTime.now());

        Long lockedUntil = locked ? 
            customer.getDigitalOtpLockedUntil().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() : null;

        Long enrolledAt = customer.getDigitalOtpEnrolledAt() != null ?
            customer.getDigitalOtpEnrolledAt().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() : null;

        return DigitalOtpStatusResponse.builder()
            .enrolled(customer.isDigitalOtpEnabled())
            .locked(locked)
            .enrolledAtTimestamp(enrolledAt)
            .lockedUntilTimestamp(lockedUntil)
            .build();
    }

    @Override
    @Transactional
    public boolean unlockDigitalOtp(String customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        customer.setDigitalOtpLockedUntil(null);
        customer.setDigitalOtpFailedAttempts(0);
        customerRepository.save(customer);

        log.info("Digital OTP unlocked for customer: {}", customerId);
        return true;
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(TOKEN_CLEANUP_DAYS);
        int deleted = usedTokenRepository.deleteExpiredTokens(cutoff);
        log.info("Cleaned up {} expired Digital OTP tokens", deleted);
    }

    /**
     * Increment failed attempts and lock if threshold exceeded
     */
    private void incrementFailedAttempts(Customer customer) {
        customer.setDigitalOtpFailedAttempts(customer.getDigitalOtpFailedAttempts() + 1);

        if (customer.getDigitalOtpFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            customer.setDigitalOtpLockedUntil(LocalDateTime.now().plusHours(LOCK_DURATION_HOURS));
            log.warn("Digital OTP locked for customer: {} due to {} failed attempts", 
                customer.getCustomerId(), MAX_FAILED_ATTEMPTS);
        }

        customerRepository.save(customer);
    }

    /**
     * Record token usage for replay protection
     */
    private void recordTokenUsage(DigitalOtpValidationRequest request) {
        String tokenHash = hashToken(request.getDigitalOtpToken());

        DigitalOtpUsedToken usedToken = DigitalOtpUsedToken.builder()
            .customerId(request.getCustomerId())
            .transactionId(request.getTransactionId())
            .tokenHash(tokenHash)
            .clientTimestamp(request.getTimestamp())
            .expiresAt(LocalDateTime.now().plusDays(TOKEN_CLEANUP_DAYS))
            .build();

        usedTokenRepository.save(usedToken);
    }

    /**
     * Build payload string for signature verification
     */
    private String buildPayload(DigitalOtpValidationRequest request) {
        return String.format("%s|%s|%s|%s|%d",
            request.getTransactionId(),
            request.getSourceAccountNumber(),
            request.getDestinationAccountNumber(),
            request.getAmount().toPlainString(),
            request.getTimestamp()
        );
    }

    /**
     * Verify digital signature using stored public key
     */
    private boolean verifySignature(String publicKeyBase64, String payload, String signatureBase64) 
            throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(payload.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(signatureBytes);
    }

    /**
     * Hash token using SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    /**
     * Build error response
     */
    private DigitalOtpValidationResponse buildErrorResponse(
            String errorCode, String message, int remainingAttempts, Long lockedUntil) {
        return DigitalOtpValidationResponse.builder()
            .valid(false)
            .errorCode(errorCode)
            .message(message)
            .remainingAttempts(remainingAttempts)
            .lockedUntilTimestamp(lockedUntil)
            .build();
    }
}
