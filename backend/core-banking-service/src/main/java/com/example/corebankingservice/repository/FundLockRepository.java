package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.FundLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundLockRepository extends JpaRepository<FundLock, String> {
    
    /**
     * Tìm lock theo reference ID
     */
    Optional<FundLock> findByReferenceIdAndStatus(String referenceId, String status);
    
    /**
     * Lấy tất cả locks đang active của một account
     */
    List<FundLock> findByAccountNumberAndStatus(String accountNumber, String status);
    
    /**
     * Tính tổng tiền đang bị lock của một account
     */
    @Query("SELECT COALESCE(SUM(fl.lockedAmount), 0) FROM FundLock fl " +
           "WHERE fl.accountNumber = :accountNumber AND fl.status = 'LOCKED'")
    BigDecimal getTotalLockedAmount(String accountNumber);
}
