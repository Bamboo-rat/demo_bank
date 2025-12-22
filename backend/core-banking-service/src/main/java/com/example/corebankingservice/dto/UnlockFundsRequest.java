package com.example.corebankingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO để unlock/release tiền đã lock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockFundsRequest {
    
    @NotBlank(message = "Lock ID is required")
    private String lockId;
    
    private String reason; // Lý do unlock (withdraw, cancel, expired, etc.)
}
