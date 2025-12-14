package com.example.accountservice.service.impl;

import com.example.accountservice.client.CoreBankingServiceClient;
import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountInfoDTO;
import com.example.accountservice.dto.response.AccountResponse;
import com.example.accountservice.dto.response.BalanceResponse;
import com.example.accountservice.entity.*;
import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.exception.*;
import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.service.AccountService;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.customer.CustomerBasicInfo;
import com.example.commonapi.dubbo.CustomerQueryDubboService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CoreBankingServiceClient coreBankingServiceClient;
    
    @DubboReference(version = "1.0.0", group = "banking-services", check = false, timeout = 5000)
    private CustomerQueryDubboService customerQueryDubboService;

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        log.info("Fetching account details for: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        AccountResponse response = accountMapper.toResponse(account);
        
        // Fetch balance from core-banking-service
        try {
            ApiResponse<BalanceResponse> balanceApiResponse = coreBankingServiceClient.getBalance(accountNumber);
            if (balanceApiResponse != null && balanceApiResponse.getData() != null) {
                BalanceResponse balanceData = balanceApiResponse.getData();
                response.setBalance(balanceData.getBalance());
                response.setAvailableBalance(balanceData.getAvailableBalance());
                log.info("Balance fetched successfully for {}: balance={}, availableBalance={}", 
                        accountNumber, balanceData.getBalance(), balanceData.getAvailableBalance());
            }
        } catch (Exception e) {
            log.error("Failed to fetch balance from core-banking for {}: {}", accountNumber, e.getMessage());
            // Continue without balance - service degradation instead of failure
            response.setBalance(BigDecimal.ZERO);
            response.setAvailableBalance(BigDecimal.ZERO);
        }
        
        return response;
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(String customerId) {
        log.info("Fetching all accounts for customer: {}", customerId);

        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            log.warn("No accounts found for customer: {}", customerId);
        }

        return accounts.stream()
                .map(account -> {
                    AccountResponse response = accountMapper.toResponse(account);
                    
                    // Fetch balance from core-banking-service for each account
                    try {
                        ApiResponse<BalanceResponse> balanceApiResponse = coreBankingServiceClient.getBalance(account.getAccountNumber());
                        if (balanceApiResponse != null && balanceApiResponse.getData() != null) {
                            BalanceResponse balanceData = balanceApiResponse.getData();
                            response.setBalance(balanceData.getBalance());
                            response.setAvailableBalance(balanceData.getAvailableBalance());
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch balance from core-banking for {}: {}", account.getAccountNumber(), e.getMessage());
                        response.setBalance(BigDecimal.ZERO);
                        response.setAvailableBalance(BigDecimal.ZERO);
                    }
                    
                    return response;
                })
                .toList();
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

    @Override
    public AccountInfoDTO getAccountInfoByNumber(String accountNumber) {
        log.info("Fetching account info for internal transfer: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        return AccountInfoDTO.builder()
                .accountNumber(account.getAccountNumber())
                .accountHolderName(getAccountHolderName(account)) 
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .bankName("Kiên Long Bank")
                .bankCode("KLB") 
                .cifNumber(account.getCifNumber())
                .isActive(account.getStatus() == AccountStatus.ACTIVE)
                .build();
    }

    /**
     * Get account holder name from Customer Service via Dubbo
     */
    private String getAccountHolderName(Account account) {
        try {
            CustomerBasicInfo customerInfo = customerQueryDubboService.getCustomerBasicInfo(account.getCustomerId());
            
            if (customerInfo != null && customerInfo.getFullName() != null) {
                log.info("Retrieved customer name via Dubbo: {} for customerId: {}", 
                        customerInfo.getFullName(), account.getCustomerId());
                return customerInfo.getFullName();
            }
            
            log.warn("Customer info not found via Dubbo for customerId: {}", account.getCustomerId());
            return "Khách hàng " + account.getCustomerId().substring(0, 8);
            
        } catch (Exception e) {
            log.error("Error calling customer service via Dubbo for customerId: {}", 
                    account.getCustomerId(), e);
            return "Khách hàng " + account.getCustomerId().substring(0, 8);
        }
    }
}
