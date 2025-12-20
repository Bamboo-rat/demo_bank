package com.example.customerservice.repository;

import com.example.customerservice.entity.DigitalOtpUsedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DigitalOtpUsedTokenRepository extends JpaRepository<DigitalOtpUsedToken, String> {

    /**
     * Check if transaction ID has been used before (replay protection)
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Cleanup expired tokens (scheduled job)
     */
    @Modifying
    @Query("DELETE FROM DigitalOtpUsedToken d WHERE d.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);
}
