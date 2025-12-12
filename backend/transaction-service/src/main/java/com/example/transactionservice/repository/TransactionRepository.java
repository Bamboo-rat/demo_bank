package com.example.transactionservice.repository;

import com.example.transactionservice.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE " +
            "t.sourceAccountId = :accountId OR t.destinationAccountId = :accountId " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountId(
            @Param("accountId") String accountId,
            Pageable pageable
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE " +
            "t.sourceAccountId = :accountId AND " +
            "t.transactionDate >= :startDate AND " +
            "t.status = 'COMPLETED'")
    BigDecimal getDailyTransactionAmount(
            @Param("accountId") String accountId,
            @Param("startDate") LocalDateTime startDate
    );
}
