package com.example.accountservice.service.impl;

import com.example.accountservice.client.CoreBankingClient;
import com.example.accountservice.client.CustomerServiceClient;
import com.example.accountservice.dto.request.OpenAccountRequest;
import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountResponse;
import com.example.accountservice.dto.response.CustomerValidationResponse;
import com.example.accountservice.entity.*;
import com.example.accountservice.entity.enums.AccountType;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.exception.*;
import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.AccountStatusHistoryRepository;
import com.example.accountservice.service.AccountNumberGenerator;

import com.example.accountservice.service.AccountService;
import com.example.commonapi.dto.ApiResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountStatusHistoryRepository historyRepository;
    private final AccountMapper accountMapper;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CoreBankingClient coreBankingClient;
    private final CustomerServiceClient customerServiceClient;

    @Value("${account.checking.min-balance:50000}")
    private BigDecimal checkingMinBalance;

    @Value("${account.savings.min-balance:100000}")
    private BigDecimal savingsMinBalance;

    @Value("${account.savings.default-interest-rate:0.035}")
    private BigDecimal defaultSavingsInterestRate;

    @Value("${account.savings.default-term:6}")
    private Integer defaultSavingsTerm;

    @Value("${account.max-accounts-per-type:5}")
    private int maxAccountsPerType;

    @Override
    @Transactional
    public AccountResponse openAccount(OpenAccountRequest request) {
        log.info("Opening new {} account for customer: {}", request.getAccountType(), request.getCustomerId());

        try {
            // 1. Validate customer exists and is active
            validateCustomer(request.getCustomerId());

            // 2. Check account limits
            validateAccountLimits(request.getCustomerId(), request.getAccountType());

            // 3. Generate account number
            String accountNumber = generateAccountNumber(request.getAccountType());

            // 4. Create account based on type
            Account account = createAccountByType(request, accountNumber);

            // 5. Set common fields
            account.setAccountNumber(accountNumber);
            account.setCustomerId(request.getCustomerId());
            account.setAccountType(request.getAccountType());
            account.setStatus(AccountStatus.ACTIVE);
            account.setCurrency(request.getCurrency());
            account.setOpenedDate(LocalDateTime.now());

            // 6. Save account
            Account savedAccount = accountRepository.save(account);

            // 7. Create initial status history
            createStatusHistory(savedAccount, null, AccountStatus.ACTIVE, "Account opened", "SYSTEM");

            // 8. Register account with Core Banking
            registerWithCoreBanking(savedAccount);

            log.info("Successfully opened account: {} for customer: {}", accountNumber, request.getCustomerId());

            return accountMapper.toResponse(savedAccount);

        } catch (BaseException e) {
            log.error("Business logic error opening account for customer: {}", request.getCustomerId(), e);
            throw e; // Re-throw business exceptions as-is
        } catch (Exception e) {
            log.error("Unexpected error opening account for customer: {}", request.getCustomerId(), e);
            throw new AccountCreationException("Failed to open account due to system error",
                Map.of("customerId", request.getCustomerId(), "accountType", request.getAccountType().toString()), e);
        }
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        log.info("Fetching account details for: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        return accountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(String customerId) {
        log.info("Fetching all accounts for customer: {}", customerId);

        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            log.warn("No accounts found for customer: {}", customerId);
        }

        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void closeAccount(String accountNumber, String customerId) {
        log.info("Closing account: {} for customer: {}", accountNumber, customerId);

        // 1. Find account with lock to prevent concurrent modifications
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        // 2. Verify ownership
        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccountAccessException("Account does not belong to customer: " + customerId);
        }

        // 3. Check if account can be closed
        validateAccountClosure(account);

        // 4. Update status
        AccountStatus oldStatus = account.getStatus();
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedDate(LocalDateTime.now());

        // 5. Save changes
        Account closedAccount = accountRepository.save(account);

        // 6. Create status history
        createStatusHistory(closedAccount, oldStatus, AccountStatus.CLOSED, "Account closed by customer", customerId);

        // 7. Notify Core Banking
        notifyCoreBankingAccountClosure(closedAccount);

        log.info("Successfully closed account: {} for customer: {}", accountNumber, customerId);
    }

    @Override
    public boolean isAccountActive(String accountNumber) {
        log.info("Checking if account is active: {}", accountNumber);

        try {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElse(null);

            return account != null && account.getStatus() == AccountStatus.ACTIVE;

        } catch (Exception e) {
            log.error("Error checking account status: {}", accountNumber, e);
            return false;
        }
    }

    // ===== Private Helper Methods =====

    private void validateCustomer(String customerId) {
        try {
            ApiResponse<CustomerValidationResponse> apiResponse = customerServiceClient.validateCustomer(
                    customerId, true, true);
            
            CustomerValidationResponse response = apiResponse.getData();

            if (!response.isValid()) {
                throw new InvalidCustomerException("Customer validation failed: " + response.getMessage(),
                    Map.of("customerId", customerId, "reason", response.getMessage()));
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate customer via REST: {}", customerId, e);
            throw new InvalidCustomerException("Unable to validate customer due to service communication error",
                Map.of("customerId", customerId), e);
        }
    }

    private void validateAccountLimits(String customerId, AccountType accountType) {
        long existingAccounts = accountRepository.countByCustomerIdAndTypeAndStatus(
                customerId, accountType, AccountStatus.ACTIVE);

        if (existingAccounts >= maxAccountsPerType) {
            throw new AccountLimitExceededException(
                    String.format("Customer already has %d active %s accounts (max: %d)",
                            existingAccounts, accountType, maxAccountsPerType),
                    Map.of("customerId", customerId, "accountType", accountType.toString(),
                           "existingAccounts", existingAccounts, "maxAllowed", maxAccountsPerType));
        }
    }

    private String generateAccountNumber(AccountType accountType) {
        int typeDigit = switch (accountType) {
            case CHECKING -> 1;
            case SAVINGS -> 2;
            case CREDIT -> 3;
            case LOAN -> 4;
            default -> 0;
        };

        return accountNumberGenerator.generate(typeDigit);
    }

    private Account createAccountByType(OpenAccountRequest request, String accountNumber) {
        return switch (request.getAccountType()) {
            case CHECKING -> new CheckingAccount();

            case SAVINGS -> {
                SavingsAccount savings = new SavingsAccount();
                savings.setInterestRate(defaultSavingsInterestRate);
                savings.setTermMonths(defaultSavingsTerm);
                yield savings;
            }

            case CREDIT -> {
                CreditAccount credit = new CreditAccount();
                credit.setCreditLimit(BigDecimal.valueOf(5000000)); // Default 5M VND
                credit.setAvailableCredit(BigDecimal.valueOf(5000000));
                credit.setStatementDate(25); // 25th of each month
                credit.setPaymentDueDate(15); // 15th of next month
                yield credit;
            }

            case LOAN -> throw new UnsupportedOperationException("Loan accounts must be created through Loan Service");
            
            default -> throw new UnsupportedOperationException("Unsupported account type: " + request.getAccountType());
        };
    }

    private void createStatusHistory(Account account, AccountStatus oldStatus,
                                     AccountStatus newStatus, String reason, String changedBy) {
        AccountStatusHistory history = new AccountStatusHistory();
        history.setAccount(account);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setReason(reason);
        history.setChangedBy(changedBy);

        historyRepository.save(history);
    }

    private void validateAccountClosure(Account account) {
        // Check if account is already closed
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException("Account is already closed: " + account.getAccountNumber());
        }

        // Note: Balance validation should be done via Core Banking Service
        // Additional checks for specific account types can be added here
    }

    private void registerWithCoreBanking(Account account) {
        try {
            coreBankingClient.registerAccount(account);
            log.info("Account {} registered with Core Banking", account.getAccountNumber());
        } catch (Exception e) {
            log.error("Failed to register account with Core Banking", e);
            throw new CoreBankingIntegrationException("Failed to register with Core Banking: " + e.getMessage());
        }
    }

    private void notifyCoreBankingAccountClosure(Account account) {
        try {
            coreBankingClient.notifyAccountClosure(account.getAccountNumber());
            log.info("Notified Core Banking about account closure: {}", account.getAccountNumber());
        } catch (Exception e) {
            log.error("Failed to notify Core Banking about account closure", e);
            // Queue for retry or manual intervention
        }
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(String accountNumber, String customerId, UpdateAccountRequest request) {
        log.info("Updating account: {} for customer: {}", accountNumber, customerId);

        // 1. Find account with lock
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        // 2. Verify ownership
        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccountAccessException("Account does not belong to customer: " + customerId);
        }

        // 3. Verify account is active
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update inactive account");
        }

        // 4. Update based on account type
        boolean updated = false;

        if (account instanceof CreditAccount creditAccount) {
            if (request.getCreditLimit() != null) {
                creditAccount.setCreditLimit(request.getCreditLimit());
                // Adjust available credit proportionally
                BigDecimal usedCredit = creditAccount.getCreditLimit().subtract(creditAccount.getAvailableCredit());
                creditAccount.setAvailableCredit(request.getCreditLimit().subtract(usedCredit));
                updated = true;
                log.info("Updated credit limit to: {}", request.getCreditLimit());
            }
        }

        if (account instanceof SavingsAccount savingsAccount) {
            if (request.getInterestRate() != null) {
                savingsAccount.setInterestRate(request.getInterestRate());
                updated = true;
                log.info("Updated interest rate to: {}", request.getInterestRate());
            }
            if (request.getTermMonths() != null) {
                savingsAccount.setTermMonths(request.getTermMonths());
                updated = true;
                log.info("Updated term to: {} months", request.getTermMonths());
            }
        }

        if (!updated) {
            log.warn("No updates applied to account: {}", accountNumber);
        }

        // 5. Save changes
        Account updatedAccount = accountRepository.save(account);

        log.info("Successfully updated account: {}", accountNumber);
        return accountMapper.toResponse(updatedAccount);
    }
}
