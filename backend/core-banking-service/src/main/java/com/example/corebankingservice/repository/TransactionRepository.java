package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.Transaction;
import com.example.corebankingservice.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    boolean existsBySourceAccountIdAndStatus(String sourceAccountId, TransactionStatus status);

    boolean existsByDestinationAccountIdAndStatus(String destinationAccountId, TransactionStatus status);
}
