package com.example.customerservice.config;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Value("${core-banking.base-url}")
    private String coreBankingBaseUrl;

    @Value("${core-banking.timeout:30}")
    private int timeout;

    @Bean
    public RestTemplate restTemplate(ObjectProvider<RestTemplateBuilder> builderProvider) {
        Duration timeoutDuration = Duration.ofSeconds(timeout);
        RestTemplateBuilder builder = builderProvider.getIfAvailable(RestTemplateBuilder::new);
        return builder
                .setConnectTimeout(timeoutDuration)
                .setReadTimeout(timeoutDuration)
                .build();
    }
}
