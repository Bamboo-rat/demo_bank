package com.example.customerservice.redis.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationSession implements Serializable {

    private static final long serialVersionUID = -6575030830098964611L;

    private String sessionId;
    private String phoneNumber;
    private RegistrationSessionStatus status;
    private RegistrationProfileData profileData;
    private RegistrationIdentityData identityData;
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
