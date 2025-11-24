package com.example.accountservice.repository;

import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(String customerId);
    List<Account> findByCustomerIdAndStatus(String customerId, AccountStatus status);
    Optional<Account> findByAccountNumberAndCustomerId(String accountNumber, String customerId);

    // Count accounts by customer and type
    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId AND a.accountType = :type AND a.status = :status")
    long countByCustomerIdAndTypeAndStatus(@Param("customerId") String customerId,
                                           @Param("type") AccountType type,
                                           @Param("status") AccountStatus status);

    // Check if customer has any active accounts
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    boolean hasActiveAccounts(@Param("customerId") String customerId);

    // Find accounts by status
    List<Account> findByStatus(AccountStatus status);

    // Native query to lock account for update (pessimistic locking)
    @Query(value = "SELECT * FROM accounts WHERE account_number = :accountNumber FOR UPDATE", nativeQuery = true)
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);
}
