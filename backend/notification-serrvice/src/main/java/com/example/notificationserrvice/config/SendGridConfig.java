package com.example.notificationserrvice.config;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "sendgrid.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SendGridConfig {
    
    private final SendGridProperties sendGridProperties;
    
    @Bean
    public SendGrid sendGrid() {
        String apiKey = sendGridProperties.getApiKey();
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SendGrid API key is not configured. Email sending will be disabled.");
            throw new IllegalStateException("SendGrid API key is required when SendGrid is enabled");
        }
        
        log.info("Initializing SendGrid client with API key: {}****", 
                apiKey.substring(0, Math.min(10, apiKey.length())));
        
        return new SendGrid(apiKey);
    }
}
