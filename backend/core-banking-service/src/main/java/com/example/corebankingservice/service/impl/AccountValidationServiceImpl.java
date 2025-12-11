package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.enums.AccountStatus;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.service.AccountValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountValidationServiceImpl implements AccountValidationService {

    private final AccountRepository accountRepository;
    private final MessageSource messageSource;

    @Override
    public void validateCanSendMoney(String accountNumber) {
        Account account = getAccount(accountNumber);
        
        if (!canDebit(account)) {
            String statusName = account.getStatus().name();
            throw new BusinessException(
                getMessage("error.account.cannot.send", new Object[]{accountNumber, statusName})
            );
        }
        
        log.debug("Account {} validated for sending money", accountNumber);
    }

    @Override
    public void validateCanReceiveMoney(String accountNumber) {
        Account account = getAccount(accountNumber);
        
        if (!canCredit(account)) {
            String statusName = account.getStatus().name();
            throw new BusinessException(
                getMessage("error.account.cannot.receive", new Object[]{accountNumber, statusName})
            );
        }
        
        log.debug("Account {} validated for receiving money", accountNumber);
    }

    @Override
    public void validateTransfer(String senderAccountNumber, String receiverAccountNumber) {
        // Validate sender can send money
        validateCanSendMoney(senderAccountNumber);
        
        // Validate receiver can receive money
        validateCanReceiveMoney(receiverAccountNumber);
        
        log.debug("Transfer validation passed: {} -> {}", senderAccountNumber, receiverAccountNumber);
    }

    @Override
    public boolean canDebit(Account account) {
        AccountStatus status = account.getStatus();
        
        // Only ACTIVE accounts can send money (debit)
        // BLOCKED, CLOSED, FROZEN, DORMANT accounts cannot send
        return status == AccountStatus.ACTIVE;
    }

    @Override
    public boolean canCredit(Account account) {
        AccountStatus status = account.getStatus();
        
        // ACTIVE and DORMANT accounts can receive money (credit)
        // BLOCKED, CLOSED, FROZEN accounts cannot receive
        return status == AccountStatus.ACTIVE || status == AccountStatus.DORMANT;
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private String getMessage(String code, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }
}
