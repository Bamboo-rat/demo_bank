package com.example.accountservice.events.producer.impl;

import com.example.accountservice.dto.dubbo.AccountSyncRequest;
import com.example.accountservice.dto.dubbo.AccountSyncResult;
import com.example.accountservice.entity.*;
import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import com.example.accountservice.events.producer.AccountSyncDubboProducer;
import com.example.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dubbo service producer implementation
 * Account-service produces/provides this service for CustomerService to consume
 * Receives account data from CustomerService orchestrator via Dubbo RPC
 */
@DubboService(version = "1.0.0", group = "banking-services")
@RequiredArgsConstructor
@Slf4j
public class AccountSyncDubboProducerImpl implements AccountSyncDubboProducer {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountSyncResult syncAccountMetadata(AccountSyncRequest request) {
        log.info("Receiving account sync via Dubbo: accountNumber={}, customerId={}", 
                request.getAccountNumber(), request.getCustomerId());

        try {
            // Check idempotency - skip if already synced
            if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
                log.warn("Account {} already exists, skipping sync", request.getAccountNumber());
                Account existing = accountRepository.findByAccountNumber(request.getAccountNumber()).orElse(null);
                return AccountSyncResult.builder()
                        .success(true)
                        .message("Account already synced (idempotent)")
                        .accountId(existing != null ? existing.getAccountId() : null)
                        .build();
            }

            // Create account entity based on type
            Account account = createAccountByType(request);

            // Set common fields
            account.setAccountNumber(request.getAccountNumber());
            account.setCustomerId(request.getCustomerId());
            account.setCifNumber(request.getCifNumber());
            account.setAccountType(AccountType.valueOf(request.getAccountType()));
            account.setCurrency(Currency.valueOf(request.getCurrency()));
            account.setStatus(AccountStatus.valueOf(request.getStatus()));
            account.setOpenedDate(request.getOpenedAt() != null ? request.getOpenedAt() : LocalDateTime.now());

            Account saved = accountRepository.save(account);
            log.info("Successfully synced account metadata: accountId={}, accountNumber={}", 
                    saved.getAccountId(), saved.getAccountNumber());

            return AccountSyncResult.builder()
                    .success(true)
                    .message("Account metadata synced successfully")
                    .accountId(saved.getAccountId())
                    .build();

        } catch (Exception e) {
            log.error("Failed to sync account metadata for accountNumber: {}", request.getAccountNumber(), e);
            return AccountSyncResult.builder()
                    .success(false)
                    .message("Sync failed: " + e.getMessage())
                    .build();
        }
    }

    private Account createAccountByType(AccountSyncRequest request) {
        AccountType accountType = AccountType.valueOf(request.getAccountType());

        return switch (accountType) {
            case CHECKING -> new CheckingAccount();
            case SAVINGS -> {
                SavingsAccount savings = new SavingsAccount();
                savings.setInterestRate(new BigDecimal("0.035")); // Default 3.5%
                savings.setTermMonths(6); // Default 6 months
                yield savings;
            }
            case CREDIT -> {
                CreditAccount credit = new CreditAccount();
                credit.setCreditLimit(new BigDecimal("5000000")); // Default 5M VND
                credit.setAvailableCredit(new BigDecimal("5000000"));
                credit.setStatementDate(25); // Day 25 of month
                credit.setPaymentDueDate(15); // Day 15 of next month
                yield credit;
            }
        };
    }
}
