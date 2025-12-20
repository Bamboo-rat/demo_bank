package com.example.commonapi.dto.digitalotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response DTO for Digital OTP enrollment status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalOtpStatusResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enrolled;
    private boolean locked;
    private Long enrolledAtTimestamp;
    private Long lockedUntilTimestamp;
}
