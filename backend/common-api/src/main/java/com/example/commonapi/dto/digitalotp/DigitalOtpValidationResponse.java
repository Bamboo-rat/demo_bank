package com.example.commonapi.dto.digitalotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response DTO for Digital OTP validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalOtpValidationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean valid;
    private String message;
    private String errorCode;           // ERROR_INVALID_PIN, ERROR_EXPIRED, ERROR_LOCKED, ERROR_REPLAY
    private int remainingAttempts;      // Number of attempts left before lock
    private Long lockedUntilTimestamp;  // Timestamp when lock expires (if locked)
}
