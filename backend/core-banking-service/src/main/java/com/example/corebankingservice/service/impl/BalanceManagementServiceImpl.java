package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.request.BalanceOperationRequest;
import com.example.corebankingservice.dto.response.BalanceOperationResponse;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.BalanceAuditLog;
import com.example.corebankingservice.entity.enums.AccountStatus;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.repository.BalanceAuditLogRepository;
import com.example.corebankingservice.service.BalanceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceManagementServiceImpl implements BalanceManagementService {

    private final AccountRepository accountRepository;
    private final BalanceAuditLogRepository auditLogRepository;
    private final MessageSource messageSource;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse debit(BalanceOperationRequest request) {
        log.info("Processing DEBIT operation: account={}, amount={}, ref={}", 
                request.getAccountNumber(), request.getAmount(), request.getTransactionReference());

        // Lock account for update (SELECT ... FOR UPDATE)
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountNumber()));

        // Validate account status
        validateAccountForDebit(account);

        // Calculate available balance
        BigDecimal availableBalance = account.getBalance().subtract(account.getHoldAmount());

        // Check sufficient funds
        if (availableBalance.compareTo(request.getAmount()) < 0) {
            throw new BusinessException(getMessage("error.insufficient.balance"));
        }

        // Store previous balance
        BigDecimal previousBalance = account.getBalance();

        // Deduct amount
        account.setBalance(previousBalance.subtract(request.getAmount()));

        // Save account
        Account savedAccount = accountRepository.save(account);

        // Calculate new available balance
        BigDecimal newAvailableBalance = savedAccount.getBalance().subtract(savedAccount.getHoldAmount());

        // Create audit log
        BalanceAuditLog auditLog = BalanceAuditLog.builder()
                .accountNumber(account.getAccountNumber())
                .operationType("DEBIT")
                .previousBalance(previousBalance)
                .operationAmount(request.getAmount())
                .newBalance(savedAccount.getBalance())
                .holdAmount(savedAccount.getHoldAmount())
                .availableBalance(newAvailableBalance)
                .currency(account.getCurrency())
                .transactionReference(request.getTransactionReference())
                .description(request.getDescription())
                .performedBy(request.getPerformedBy())
                .build();

        auditLogRepository.save(auditLog);

        log.info("DEBIT completed: account={}, previous={}, new={}, available={}", 
                account.getAccountNumber(), previousBalance, savedAccount.getBalance(), newAvailableBalance);

        return BalanceOperationResponse.builder()
                .accountNumber(savedAccount.getAccountNumber())
                .previousBalance(previousBalance)
                .operationAmount(request.getAmount())
                .newBalance(savedAccount.getBalance())
                .availableBalance(newAvailableBalance)
                .currency(savedAccount.getCurrency())
                .transactionReference(request.getTransactionReference())
                .operationType("DEBIT")
                .operationTime(LocalDateTime.now())
                .message("Debit operation completed successfully")
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse credit(BalanceOperationRequest request) {
        log.info("Processing CREDIT operation: account={}, amount={}, ref={}", 
                request.getAccountNumber(), request.getAmount(), request.getTransactionReference());

        // Lock account for update
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountNumber()));

        // Validate account status (can receive credits even if dormant)
        validateAccountForCredit(account);

        // Store previous balance
        BigDecimal previousBalance = account.getBalance();

        // Add amount
        account.setBalance(previousBalance.add(request.getAmount()));

        // Save account
        Account savedAccount = accountRepository.save(account);

        // Calculate available balance
        BigDecimal availableBalance = savedAccount.getBalance().subtract(savedAccount.getHoldAmount());

        // Create audit log
        BalanceAuditLog auditLog = BalanceAuditLog.builder()
                .accountNumber(account.getAccountNumber())
                .operationType("CREDIT")
                .previousBalance(previousBalance)
                .operationAmount(request.getAmount())
                .newBalance(savedAccount.getBalance())
                .holdAmount(savedAccount.getHoldAmount())
                .availableBalance(availableBalance)
                .currency(account.getCurrency())
                .transactionReference(request.getTransactionReference())
                .description(request.getDescription())
                .performedBy(request.getPerformedBy())
                .build();

        auditLogRepository.save(auditLog);

        log.info("CREDIT completed: account={}, previous={}, new={}, available={}", 
                account.getAccountNumber(), previousBalance, savedAccount.getBalance(), availableBalance);

        return BalanceOperationResponse.builder()
                .accountNumber(savedAccount.getAccountNumber())
                .previousBalance(previousBalance)
                .operationAmount(request.getAmount())
                .newBalance(savedAccount.getBalance())
                .availableBalance(availableBalance)
                .currency(savedAccount.getCurrency())
                .transactionReference(request.getTransactionReference())
                .operationType("CREDIT")
                .operationTime(LocalDateTime.now())
                .message("Credit operation completed successfully")
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse holdAmount(String accountNumber, BigDecimal amount, String transactionReference) {
        log.info("Processing HOLD operation: account={}, amount={}, ref={}", accountNumber, amount, transactionReference);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        validateAccountForDebit(account);

        // Check available balance
        BigDecimal availableBalance = account.getBalance().subtract(account.getHoldAmount());
        if (availableBalance.compareTo(amount) < 0) {
            throw new BusinessException(getMessage("error.insufficient.balance"));
        }

        BigDecimal previousHoldAmount = account.getHoldAmount();
        account.setHoldAmount(previousHoldAmount.add(amount));

        Account savedAccount = accountRepository.save(account);
        BigDecimal newAvailableBalance = savedAccount.getBalance().subtract(savedAccount.getHoldAmount());

        // Create audit log
        BalanceAuditLog auditLog = BalanceAuditLog.builder()
                .accountNumber(accountNumber)
                .operationType("HOLD")
                .previousBalance(savedAccount.getBalance())
                .operationAmount(amount)
                .newBalance(savedAccount.getBalance())
                .holdAmount(savedAccount.getHoldAmount())
                .availableBalance(newAvailableBalance)
                .currency(savedAccount.getCurrency())
                .transactionReference(transactionReference)
                .description("Amount held for pending transaction")
                .build();

        auditLogRepository.save(auditLog);

        log.info("HOLD completed: account={}, hold_amount={}, available={}", 
                accountNumber, savedAccount.getHoldAmount(), newAvailableBalance);

        return BalanceOperationResponse.builder()
                .accountNumber(accountNumber)
                .previousBalance(savedAccount.getBalance())
                .operationAmount(amount)
                .newBalance(savedAccount.getBalance())
                .availableBalance(newAvailableBalance)
                .currency(savedAccount.getCurrency())
                .transactionReference(transactionReference)
                .operationType("HOLD")
                .operationTime(LocalDateTime.now())
                .message("Amount held successfully")
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse releaseHoldAmount(String accountNumber, BigDecimal amount, String transactionReference) {
        log.info("Processing RELEASE_HOLD operation: account={}, amount={}, ref={}", accountNumber, amount, transactionReference);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        if (account.getHoldAmount().compareTo(amount) < 0) {
            throw new BusinessException("Hold amount is less than release amount");
        }

        BigDecimal previousHoldAmount = account.getHoldAmount();
        account.setHoldAmount(previousHoldAmount.subtract(amount));

        Account savedAccount = accountRepository.save(account);
        BigDecimal newAvailableBalance = savedAccount.getBalance().subtract(savedAccount.getHoldAmount());

        // Create audit log
        BalanceAuditLog auditLog = BalanceAuditLog.builder()
                .accountNumber(accountNumber)
                .operationType("RELEASE_HOLD")
                .previousBalance(savedAccount.getBalance())
                .operationAmount(amount)
                .newBalance(savedAccount.getBalance())
                .holdAmount(savedAccount.getHoldAmount())
                .availableBalance(newAvailableBalance)
                .currency(savedAccount.getCurrency())
                .transactionReference(transactionReference)
                .description("Hold amount released")
                .build();

        auditLogRepository.save(auditLog);

        log.info("RELEASE_HOLD completed: account={}, hold_amount={}, available={}", 
                accountNumber, savedAccount.getHoldAmount(), newAvailableBalance);

        return BalanceOperationResponse.builder()
                .accountNumber(accountNumber)
                .previousBalance(savedAccount.getBalance())
                .operationAmount(amount)
                .newBalance(savedAccount.getBalance())
                .availableBalance(newAvailableBalance)
                .currency(savedAccount.getCurrency())
                .transactionReference(transactionReference)
                .operationType("RELEASE_HOLD")
                .operationTime(LocalDateTime.now())
                .message("Hold amount released successfully")
                .build();
    }

    private void validateAccountForDebit(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(getMessage("error.account.not.active.for.debit"));
        }
        if (account.isAmlFlag()) {
            throw new BusinessException(getMessage("error.account.aml.flag"));
        }
    }

    private void validateAccountForCredit(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(getMessage("error.account.closed"));
        }
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException(getMessage("error.account.blocked"));
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BusinessException(getMessage("error.account.frozen"));
        }
    }

    private String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }
}
