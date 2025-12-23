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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

        // Store TOTP secret, PIN hash (Base64), and salt
        customer.setDigitalOtpSecret(request.getDigitalOtpSecret());
        customer.setDigitalPinHash(Base64.getDecoder().decode(request.getDigitalPinHash()));
        customer.setDigitalOtpSalt(request.getSalt());
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
        if (customer.getDigitalOtpEnabled() == null || !customer.getDigitalOtpEnabled()) {
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

        // Verify PIN hash first
        try {
            if (request.getPinHashCurrent() == null || customer.getDigitalOtpSalt() == null) {
                log.warn("PIN hash or salt missing for customer: {}", request.getCustomerId());
                incrementFailedAttempts(customer);
                return buildErrorResponse("ERROR_MISSING_PIN", 
                    "PIN verification required", 
                    MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
            }

            byte[] expectedPinHash = customer.getDigitalPinHash();
            byte[] providedPinHash = Base64.getDecoder().decode(request.getPinHashCurrent());
            
            if (!MessageDigest.isEqual(expectedPinHash, providedPinHash)) {
                log.warn("Invalid PIN hash for customer: {}", request.getCustomerId());
                incrementFailedAttempts(customer);
                return buildErrorResponse("ERROR_INVALID_PIN", 
                    "Invalid PIN", 
                    MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
            }
        } catch (Exception e) {
            log.error("Error verifying PIN", e);
            incrementFailedAttempts(customer);
            return buildErrorResponse("ERROR_PIN_VERIFICATION_FAILED", 
                "PIN verification failed", 
                MAX_FAILED_ATTEMPTS - customer.getDigitalOtpFailedAttempts(), null);
        }

        // Calculate time slice from timestamp
        long timeSlice = request.getTimestamp() / 30000L;
        
        // Verify TOTP token with ±1 time slice tolerance
        try {
            boolean tokenValid = false;
            String matchedPayload = null;
            String matchedToken = null;
            
            for (int offset = -1; offset <= 1; offset++) {
                String payload = buildPayload(request, timeSlice + offset);
                String expectedToken = generateTotpToken(
                    customer.getDigitalOtpSecret(),
                    payload
                );
                
                log.debug("Testing offset {}: Expected token: {}, Payload: {}", offset, expectedToken, payload);
                
                if (expectedToken.equals(request.getDigitalOtpToken())) {
                    tokenValid = true;
                    matchedPayload = payload;
                    matchedToken = expectedToken;
                    log.info("OTP token matched with offset: {}", offset);
                    break;
                }
            }

            if (!tokenValid) {
                log.warn("Invalid TOTP token for transaction: {}. No match found in time window.", 
                    request.getTransactionId());
                incrementFailedAttempts(customer);
                return buildErrorResponse("ERROR_INVALID_TOKEN", 
                    "Invalid Digital OTP token", 
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
            .enrolled(Boolean.TRUE.equals(customer.getDigitalOtpEnabled()))
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
        int currentAttempts = customer.getDigitalOtpFailedAttempts() != null ? customer.getDigitalOtpFailedAttempts() : 0;
        customer.setDigitalOtpFailedAttempts(currentAttempts + 1);

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
     * Build payload string for TOTP generation (must match frontend exactly)
     */
    private String buildPayload(DigitalOtpValidationRequest request, long timeSlice) {
        String bankCode = request.getDestinationBankCode();
        if (bankCode == null || bankCode.isEmpty()) {
            bankCode = "KIENLONG";  // Default for internal transfers
        }
        
        // Format amount to 2 decimal places with dot separator (US format) to match frontend's toFixed(2)
        String formattedAmount = String.format(Locale.US, "%.2f", request.getAmount());
        
        return String.format("%s|%s|%s|%s|%s|%d",
            request.getTransactionId(),
            request.getSourceAccountNumber(),
            request.getDestinationAccountNumber(),
            bankCode,
            formattedAmount,
            timeSlice
        );
    }

    /**
     * Generate TOTP token using HMAC-SHA256 (must match frontend exactly)
     */
    private String generateTotpToken(String secretBase64, String payload) throws Exception {
        byte[] secretBytes = Base64.getDecoder().decode(secretBase64);
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        
        // HMAC-SHA256
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
        hmac.init(keySpec);
        byte[] hash = hmac.doFinal(payloadBytes);
        
        // Convert to 6-digit code
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                   | ((hash[offset + 1] & 0xFF) << 16)
                   | ((hash[offset + 2] & 0xFF) << 8)
                   | (hash[offset + 3] & 0xFF);
        
        int otp = binary % 1000000;
        return String.format("%06d", otp);
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
