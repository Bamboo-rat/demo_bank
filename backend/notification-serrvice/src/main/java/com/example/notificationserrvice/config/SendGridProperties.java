package com.example.notificationserrvice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sendgrid")
@Getter
@Setter
public class SendGridProperties {

    private String apiKey;
    private From from = new From();

    private boolean enabled = true;
    
    @Getter
    @Setter
    public static class From {
     
        private String email = "langyvu@gmail.com";
        private String name = "KLB Bank";
    }
}
