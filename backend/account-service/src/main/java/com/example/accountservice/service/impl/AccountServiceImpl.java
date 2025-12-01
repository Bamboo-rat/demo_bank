package com.example.accountservice.service.impl;

import com.example.accountservice.client.CoreBankingClient;
import com.example.accountservice.client.CustomerServiceClient;
import com.example.accountservice.dto.request.OpenAccountRequest;
import com.example.accountservice.dto.request.OpenAccountCoreRequest;
import com.example.accountservice.dto.request.AccountLifecycleActionRequest;
import com.example.accountservice.dto.response.AccountDetailResponse;
import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountResponse;
import com.example.accountservice.dto.response.CustomerValidationResponse;
import com.example.accountservice.entity.*;
import com.example.accountservice.entity.enums.AccountType;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.exception.*;
import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.AccountRepository;

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
    private final AccountMapper accountMapper;
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
            // 1. Validate customer and get CIF
            CustomerValidationResponse customer = fetchValidatedCustomer(request.getCustomerId());

            // 2. Check account limits (local policy)
            validateAccountLimits(request.getCustomerId(), request.getAccountType());

            // 3. Delegate to Core for opening (CHECKING/SAVINGS/CREDIT)
            if (request.getAccountType() == AccountType.LOAN) {
                throw new UnsupportedOperationException("Loan accounts must be created through Loan Service");
            }

            OpenAccountCoreRequest coreReq = OpenAccountCoreRequest.builder()
                    .cifNumber(customer.getCifNumber())
                    .accountType(request.getAccountType())
                    .currency(request.getCurrency())
                    .createdBy("ACCOUNT-SERVICE")
                    .description("Opened via Account Service")
                    .build();

            AccountDetailResponse coreResp = coreBankingClient.openAccount(coreReq);

            // 4. Persist a local shadow record (optional)
            Account local;
            switch (request.getAccountType()) {
                case CHECKING -> local = new CheckingAccount();
                case SAVINGS -> {
                    SavingsAccount s = new SavingsAccount();
                    s.setInterestRate(defaultSavingsInterestRate);
                    s.setTermMonths(defaultSavingsTerm);
                    local = s;
                }
                case CREDIT -> {
                    CreditAccount c = new CreditAccount();
                    c.setCreditLimit(BigDecimal.valueOf(5_000_000));
                    c.setAvailableCredit(BigDecimal.valueOf(5_000_000));
                    c.setStatementDate(25);
                    c.setPaymentDueDate(15);
                    local = c;
                }
                default -> throw new UnsupportedOperationException("Unsupported account type: " + request.getAccountType());
            }

            local.setAccountNumber(coreResp.getAccountNumber());
            local.setCustomerId(request.getCustomerId());
            local.setAccountType(request.getAccountType());
            local.setStatus(AccountStatus.ACTIVE);
            local.setCurrency(request.getCurrency());
            local.setOpenedDate(LocalDateTime.now());

            Account saved = accountRepository.save(local);

            log.info("Successfully opened account {} for customer {} via Core", saved.getAccountNumber(), request.getCustomerId());
            return accountMapper.toResponse(saved);

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

        // 3. Delegate to Core Banking to perform closure with full validations
        coreBankingClient.closeAccount(accountNumber, AccountLifecycleActionRequest.builder()
            .reason("Customer requested closure")
            .performedBy(customerId)
            .build());

        // 4. Mirror status locally
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedDate(LocalDateTime.now());
        accountRepository.save(account);

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

    private CustomerValidationResponse fetchValidatedCustomer(String customerId) {
        ApiResponse<CustomerValidationResponse> apiResponse = customerServiceClient.validateCustomer(
                customerId, true, true);
        CustomerValidationResponse response = apiResponse.getData();
        if (response == null || !response.isValid()) {
            throw new InvalidCustomerException("Customer validation failed", Map.of("customerId", customerId));
        }
        return response;
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
