package com.example.transactionservice.service.impl;

import com.example.transactionservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * OTP Service Implementation using Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final String OTP_PREFIX = "otp:transaction:";
    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(5); // 5 minutes
    private static final int MAX_ATTEMPTS = 3;
    
    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp(String transactionId, String phoneNumber) {
        String otp = generateRandomOtp();
        String redisKey = OTP_PREFIX + transactionId;
        
        // Store OTP in Redis with expiration
        redisTemplate.opsForValue().set(
            redisKey, 
            otp, 
            OTP_EXPIRATION
        );
        
        // Store attempt counter
        String attemptKey = redisKey + ":attempts";
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_EXPIRATION);
        
        // TODO: [KAFKA] Send OTP via Notification Service
        // kafkaTemplate.send("notification-topic", NotificationEvent.builder()
        //     .type(NotificationType.OTP)
        //     .recipient(phoneNumber)
        //     .content("Your OTP: " + otp)
        //     .build());
        
        // For testing: Log OTP to console
        log.info("==============================================");
        log.info("🔐 OTP GENERATED for Transaction: {}", transactionId);
        log.info("📱 Phone: {}", maskPhoneNumber(phoneNumber));
        log.info("🔢 OTP Code: {}", otp);
        log.info("⏰ Valid for: {} minutes", OTP_EXPIRATION.toMinutes());
        log.info("==============================================");
        
        return otp; // Return for testing only, should not return in production
    }

    @Override
    public boolean validateOtp(String transactionId, String otp) {
        String redisKey = OTP_PREFIX + transactionId;
        String attemptKey = redisKey + ":attempts";
        
        // Check attempts
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        if (attemptsStr != null) {
            int attempts = Integer.parseInt(attemptsStr);
            if (attempts >= MAX_ATTEMPTS) {
                log.warn("❌ Max OTP attempts reached for transaction: {}", transactionId);
                invalidateOtp(transactionId);
                return false;
            }
        }
        
        // Get stored OTP
        String storedOtp = redisTemplate.opsForValue().get(redisKey);
        
        if (storedOtp == null) {
            log.warn("❌ OTP not found or expired for transaction: {}", transactionId);
            return false;
        }
        
        // Validate OTP
        boolean isValid = storedOtp.equals(otp);
        
        if (!isValid) {
            // Increment attempts
            redisTemplate.opsForValue().increment(attemptKey);
            log.warn("❌ Invalid OTP for transaction: {}", transactionId);
            return false;
        }
        
        log.info("✅ OTP validated successfully for transaction: {}", transactionId);
        return true;
    }

    @Override
    public void invalidateOtp(String transactionId) {
        String redisKey = OTP_PREFIX + transactionId;
        String attemptKey = redisKey + ":attempts";
        
        redisTemplate.delete(redisKey);
        redisTemplate.delete(attemptKey);
        
        log.info("🗑️ OTP invalidated for transaction: {}", transactionId);
    }

    @Override
    public boolean hasValidOtp(String transactionId) {
        String redisKey = OTP_PREFIX + transactionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateRandomOtp() {
        int otp = secureRandom.nextInt(900000) + 100000; // 100000 to 999999
        return String.valueOf(otp);
    }

    /**
     * Mask phone number for security (show last 4 digits only)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
