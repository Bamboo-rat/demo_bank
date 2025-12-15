package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.BalanceAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BalanceAuditLogRepository extends JpaRepository<BalanceAuditLog, String> {

    /**
     * Find audit logs by account number
     */
    List<BalanceAuditLog> findByAccountNumberOrderByOperationTimeDesc(String accountNumber);

    /**
     * Find audit logs by account number with pagination
     */
    Page<BalanceAuditLog> findByAccountNumber(String accountNumber, Pageable pageable);

    /**
     * Find by transaction reference
     */
    List<BalanceAuditLog> findByTransactionReference(String transactionReference);

    /**
     * Find by transaction reference and operation type
     */
    List<BalanceAuditLog> findByTransactionReferenceAndOperationType(String transactionReference, String operationType);

    /**
     * Find audit logs within date range
     */
    @Query("SELECT b FROM BalanceAuditLog b WHERE b.accountNumber = :accountNumber " +
           "AND b.operationTime BETWEEN :startDate AND :endDate " +
           "ORDER BY b.operationTime DESC")
    List<BalanceAuditLog> findByAccountAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find by operation type
     */
    List<BalanceAuditLog> findByAccountNumberAndOperationType(String accountNumber, String operationType);
}
