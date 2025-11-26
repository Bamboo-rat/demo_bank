package com.example.customerservice.redis;

import com.example.customerservice.redis.model.RegistrationSession;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RegistrationSessionRepository {

    private static final String SESSION_KEY_PREFIX = "registration:session:";
    private static final String OTP_KEY_PREFIX = "registration:otp:";

    private final RedisTemplate<String, RegistrationSession> registrationSessionRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public Optional<RegistrationSession> findSession(String phoneNumber) {
        RegistrationSession session = registrationSessionRedisTemplate.opsForValue().get(sessionKey(phoneNumber));
        return Optional.ofNullable(session);
    }

    public void saveSession(RegistrationSession session, Duration ttl) {
        ValueOperations<String, RegistrationSession> ops = registrationSessionRedisTemplate.opsForValue();
        ops.set(sessionKey(session.getPhoneNumber()), session, ttl);
    }

    public void deleteSession(String phoneNumber) {
        registrationSessionRedisTemplate.delete(sessionKey(phoneNumber));
    }

    public boolean sessionExists(String phoneNumber) {
        return Boolean.TRUE.equals(registrationSessionRedisTemplate.hasKey(sessionKey(phoneNumber)));
    }

    public void saveOtpCode(String phoneNumber, String otp, Duration ttl) {
        stringRedisTemplate.opsForValue().set(otpKey(phoneNumber), otp, ttl);
    }

    public Optional<String> getOtpCode(String phoneNumber) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(otpKey(phoneNumber)));
    }

    public void deleteOtpCode(String phoneNumber) {
        stringRedisTemplate.delete(otpKey(phoneNumber));
    }

    private String sessionKey(String phoneNumber) {
        return SESSION_KEY_PREFIX + phoneNumber;
    }

    private String otpKey(String phoneNumber) {
        return OTP_KEY_PREFIX + phoneNumber;
    }
}
