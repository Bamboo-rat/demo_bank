package com.example.customerservice.scheduler;

import com.example.customerservice.service.DigitalOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalOtpCleanupScheduler {

    private final DigitalOtpService digitalOtpService;

    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    public void cleanupExpiredTokens() {
        log.info("Starting Digital OTP token cleanup");
        try {
            digitalOtpService.cleanupExpiredTokens();
            log.info("Digital OTP token cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during Digital OTP token cleanup", e);
        }
    }
}
