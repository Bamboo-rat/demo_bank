package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.request.AccountLifecycleActionRequest;
import com.example.corebankingservice.dto.request.AccountStatusUpdateRequest;
import com.example.corebankingservice.dto.request.OpenAccountCoreRequest;
import com.example.corebankingservice.dto.response.AccountDetailResponse;
import com.example.corebankingservice.dto.response.AccountStatusHistoryResponse;
import com.example.corebankingservice.dto.response.AccountStatusResponse;
import com.example.corebankingservice.dto.response.BalanceResponse;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.AccountStatusHistory;
import com.example.corebankingservice.entity.enums.AccountStatus;
import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.mapper.AccountMapper;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.repository.AccountStatusHistoryRepository;
import com.example.corebankingservice.repository.TransactionRepository;
import com.example.corebankingservice.service.AccountLifecycleService;
import com.example.corebankingservice.service.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountLifecycleServiceImpl implements AccountLifecycleService {

    private final AccountRepository accountRepository;
    private final AccountStatusHistoryRepository historyRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final AccountNumberGenerator accountNumberGenerator;
    private final MessageSource messageSource;

    @Override
    public AccountDetailResponse openAccount(OpenAccountCoreRequest request) {
        String accountNumber = accountNumberGenerator.generate(request.getAccountType());
        Account account = accountMapper.toEntity(request);
        account.setAccountNumber(accountNumber);
        account.setOpenedAt(LocalDateTime.now());
        account.setStatus(AccountStatus.ACTIVE);

        Account saved = accountRepository.save(account);
        recordHistory(saved.getAccountNumber(), null, AccountStatus.ACTIVE,
                Objects.requireNonNullElse(request.getDescription(), "Account opened"),
                Objects.requireNonNullElse(request.getCreatedBy(), "SYSTEM"));

        log.info("Account {} opened for CIF {}", saved.getAccountNumber(), saved.getCifNumber());
        return accountMapper.toDetail(saved);
    }

    @Override
    public AccountDetailResponse closeAccount(String accountNumber, AccountLifecycleActionRequest request) {
        Account account = getAccountForUpdate(accountNumber);
        validateClosable(account);

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(AccountStatus.CLOSED);

        Account saved = accountRepository.save(account);
        recordHistory(accountNumber, previousStatus, AccountStatus.CLOSED, request.getReason(), request.getPerformedBy());

        log.info("Account {} closed by {}", accountNumber, request.getPerformedBy());
        return accountMapper.toDetail(saved);
    }

    @Override
    public AccountDetailResponse freezeAccount(String accountNumber, AccountLifecycleActionRequest request) {
        Account account = getAccountForUpdate(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(getMessage("error.account.cannot.freeze.closed"));
        }
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException(getMessage("error.account.cannot.freeze.blocked"));
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            return accountMapper.toDetail(account);
        }

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(AccountStatus.FROZEN);

        Account saved = accountRepository.save(account);
        recordHistory(accountNumber, previousStatus, AccountStatus.FROZEN, request.getReason(), request.getPerformedBy());

        log.info("Account {} frozen by {}", accountNumber, request.getPerformedBy());
        return accountMapper.toDetail(saved);
    }

    @Override
    public AccountDetailResponse unfreezeAccount(String accountNumber, AccountLifecycleActionRequest request) {
        Account account = getAccountForUpdate(accountNumber);
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new BusinessException(getMessage("error.account.not.frozen"));
        }

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(AccountStatus.ACTIVE);

        Account saved = accountRepository.save(account);
        recordHistory(accountNumber, previousStatus, AccountStatus.ACTIVE, request.getReason(), request.getPerformedBy());

        log.info("Account {} unfrozen by {}", accountNumber, request.getPerformedBy());
        return accountMapper.toDetail(saved);
    }

    @Override
    public AccountDetailResponse blockAccount(String accountNumber, AccountLifecycleActionRequest request) {
        Account account = getAccountForUpdate(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(getMessage("error.account.cannot.block.closed"));
        }
        if (account.getStatus() == AccountStatus.BLOCKED) {
            return accountMapper.toDetail(account);
        }

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(AccountStatus.BLOCKED);

        Account saved = accountRepository.save(account);
        recordHistory(accountNumber, previousStatus, AccountStatus.BLOCKED, request.getReason(), request.getPerformedBy());

        log.info("Account {} blocked by {}", accountNumber, request.getPerformedBy());
        return accountMapper.toDetail(saved);
    }

    @Override
    public AccountDetailResponse unblockAccount(String accountNumber, AccountLifecycleActionRequest request) {
        Account account = getAccountForUpdate(accountNumber);
        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new BusinessException(getMessage("error.account.not.blocked"));
        }

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(AccountStatus.ACTIVE);

        Account saved = accountRepository.save(account);
        recordHistory(accountNumber, previousStatus, AccountStatus.ACTIVE, request.getReason(), request.getPerformedBy());

        log.info("Account {} unblocked by {}", accountNumber, request.getPerformedBy());
        return accountMapper.toDetail(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatusResponse getAccountStatus(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return accountMapper.toStatus(account);
    }

    @Override
    public AccountDetailResponse updateAccountStatus(String accountNumber, AccountStatusUpdateRequest request) {
        Account account = getAccountForUpdate(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(getMessage("error.account.status.closed"));
        }

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(request.getStatus());

        Account saved = accountRepository.save(account);
        recordHistory(accountNumber, previousStatus, request.getStatus(), request.getReason(), request.getPerformedBy());

        log.info("Account {} status updated to {} by {}", accountNumber, request.getStatus(), request.getPerformedBy());
        return accountMapper.toDetail(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailResponse getAccountDetail(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return accountMapper.toDetail(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountStatusHistoryResponse> getStatusHistory(String accountNumber) {
        List<AccountStatusHistory> history = historyRepository.findByAccountNumberOrderByChangedAtDesc(accountNumber);
        return history.stream().map(accountMapper::toHistory).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getAvailableBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        
        return accountMapper.toBalance(account);
    }

    private Account getAccountForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private void validateClosable(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(getMessage("error.account.already.closed"));
        }
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException(getMessage("error.account.cannot.close.blocked"));
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(getMessage("error.account.cannot.close.balance"));
        }
        if (account.getHoldAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(getMessage("error.account.cannot.close.hold"));
        }
        if (account.isAmlFlag()) {
            throw new BusinessException(getMessage("error.account.cannot.close.aml"));
        }
        boolean hasPending = transactionRepository.existsBySourceAccountIdAndStatus(account.getAccountId(), TransactionStatus.PENDING)
                || transactionRepository.existsByDestinationAccountIdAndStatus(account.getAccountId(), TransactionStatus.PENDING);
        if (hasPending) {
            throw new BusinessException(getMessage("error.account.cannot.close.pending"));
        }
    }

    private void recordHistory(String accountNumber, AccountStatus previousStatus, AccountStatus currentStatus,
                               String reason, String performedBy) {
        AccountStatusHistory history = AccountStatusHistory.builder()
                .accountNumber(accountNumber)
                .previousStatus(previousStatus)
                .currentStatus(currentStatus)
                .reason(reason)
                .performedBy(performedBy)
                .build();
        historyRepository.save(history);
    }

    private String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }
}
