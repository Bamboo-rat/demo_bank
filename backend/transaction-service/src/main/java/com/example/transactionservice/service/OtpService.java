package com.example.transactionservice.service;

/**
 * OTP Service for transaction verification
 * Uses Redis for temporary storage
 */
public interface OtpService {

    /**
     * Generate and store OTP for transaction
     * @param transactionId transaction identifier
     * @param phoneNumber user's phone number
     * @return generated OTP (for testing/logging only)
     */
    String generateOtp(String transactionId, String phoneNumber);

    /**
     * Validate OTP for transaction
     * @param transactionId transaction identifier
     * @param otp OTP code to validate
     * @return true if valid, false otherwise
     */
    boolean validateOtp(String transactionId, String otp);

    /**
     * Invalidate/remove OTP after use
     * @param transactionId transaction identifier
     */
    void invalidateOtp(String transactionId);

    /**
     * Check if OTP exists and not expired
     * @param transactionId transaction identifier
     * @return true if exists
     */
    boolean hasValidOtp(String transactionId);
}
