package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.SavingsAccount;
import com.example.corebankingservice.entity.enums.SavingsAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, String> {

    Optional<SavingsAccount> findBySavingsAccountId(String savingsAccountId);

    long countBySourceAccountNumberAndStatus(String sourceAccountNumber, SavingsAccountStatus status);
}
