package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.enums.AccountStatus;
import com.example.corebankingservice.entity.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCifNumber(String cifNumber);

    long countByCifNumberAndAccountTypeAndStatusNot(String cifNumber, AccountType accountType, AccountStatus status);
}
