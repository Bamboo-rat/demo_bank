package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.request.BalanceOperationRequest;
import com.example.corebankingservice.dto.request.SavingsAccountCreationRequest;
import com.example.corebankingservice.dto.request.SavingsWithdrawalRequest;
import com.example.corebankingservice.dto.request.TransactionRecordRequest;
import com.example.corebankingservice.dto.response.BalanceOperationResponse;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.FundLock;
import com.example.corebankingservice.entity.SavingsAccount;
import com.example.corebankingservice.entity.Transaction;
import com.example.corebankingservice.entity.enums.SavingsAccountStatus;
import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.entity.enums.TransactionType;
import com.example.corebankingservice.entity.enums.AccountType;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ErrorCode;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.repository.FundLockRepository;
import com.example.corebankingservice.repository.SavingsAccountRepository;
import com.example.corebankingservice.service.AccountNumberGenerator;
import com.example.corebankingservice.service.BalanceManagementService;
import com.example.corebankingservice.service.SavingsAccountService;
import com.example.corebankingservice.service.TransactionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation ghi nhận dữ liệu tiết kiệm vào Core Banking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsAccountServiceImpl implements SavingsAccountService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final AccountRepository accountRepository;
    private final FundLockRepository fundLockRepository;
    private final BalanceManagementService balanceManagementService;
    private final TransactionRecordService transactionRecordService;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    @Transactional
    public String createSavingsAccount(SavingsAccountCreationRequest request) {
        log.info("[CORE-SAVINGS-CREATE] Saving savings account info: id={}, sourceAccount={}",
                request.getSavingsAccountId(), request.getSourceAccountNumber());

        Optional<SavingsAccount> existing = savingsAccountRepository.findById(request.getSavingsAccountId());
        if (existing.isPresent()) {
            log.info("[CORE-SAVINGS-CREATE] Savings account already exists, returning existing record");
            return existing.get().getSavingsAccountId();
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND,
                        "Source account not found: " + request.getSourceAccountNumber()));

        String cifNumber = request.getCifNumber() != null ? request.getCifNumber() : sourceAccount.getCifNumber();

        FundLock fundLock = fundLockRepository.findByReferenceIdAndStatus(request.getSavingsAccountId(), "LOCKED")
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST,
                        "No active fund lock found for savings account: " + request.getSavingsAccountId()));

        // Generate savings account number
        String savingsAccountNumber = accountNumberGenerator.generate(AccountType.SAVINGS);
        log.info("[CORE-SAVINGS-CREATE] Generated savings account number: {}", savingsAccountNumber);

        SavingsAccount savingsAccount = SavingsAccount.builder()
                .savingsAccountId(request.getSavingsAccountId())
                .savingsAccountNumber(savingsAccountNumber)
                .customerId(request.getCustomerId())
                .cifNumber(cifNumber)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .principalAmount(request.getPrincipalAmount())
                .interestRate(request.getInterestRate())
                .tenor(request.getTenor())
                .tenorMonths(request.getTenorMonths())
                .interestPaymentMethod(request.getInterestPaymentMethod())
                .autoRenewType(request.getAutoRenewType())
                .startDate(request.getStartDate())
                .maturityDate(request.getMaturityDate())
                .description(request.getDescription())
                .status(SavingsAccountStatus.ACTIVE)
                .fundLockId(fundLock.getLockId())
                .build();

        savingsAccountRepository.save(savingsAccount);
        log.info("[CORE-SAVINGS-CREATE] Savings account persisted successfully with number: {}", savingsAccountNumber);
        return savingsAccountNumber;
    }

    @Override
    @Transactional
    public String withdrawSavings(SavingsWithdrawalRequest request) {
        log.info("[CORE-SAVINGS-WITHDRAW] Processing withdrawal for savingsId={}", request.getSavingsAccountId());

        SavingsAccount savingsAccount = savingsAccountRepository.findById(request.getSavingsAccountId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST,
                        "Savings account not found: " + request.getSavingsAccountId()));

        if (savingsAccount.getStatus() != SavingsAccountStatus.ACTIVE) {
            log.error("[CORE-SAVINGS-WITHDRAW] Invalid status {} for savingsId={}",
                    savingsAccount.getStatus(), savingsAccount.getSavingsAccountId());
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Savings account is not active and cannot be withdrawn");
        }

        if (!savingsAccount.getSourceAccountNumber().equals(request.getSourceAccountNumber())) {
            log.error("[CORE-SAVINGS-WITHDRAW] Source account mismatch. Expected={}, actual={}",
                    savingsAccount.getSourceAccountNumber(), request.getSourceAccountNumber());
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Source account does not match savings record");
        }

        LocalDateTime withdrawalTime = Optional.ofNullable(request.getWithdrawnAt()).orElse(LocalDateTime.now());
        savingsAccount.setStatus(SavingsAccountStatus.CANCELLED);
        savingsAccount.setWithdrawnAt(withdrawalTime);
        savingsAccount.setPenaltyAmount(request.getPenaltyAmount());
        savingsAccount.setPaidInterestAmount(request.getInterestAmount());

        savingsAccountRepository.save(savingsAccount);
        log.info("[CORE-SAVINGS-WITHDRAW] Savings account marked as cancelled");

        String transactionReference = "SAVINGS-INT-" + request.getSavingsAccountId();
        String transactionId = null;

        if (request.getInterestAmount() != null && request.getInterestAmount().compareTo(BigDecimal.ZERO) > 0) {
            BalanceOperationRequest creditRequest = BalanceOperationRequest.builder()
                    .accountNumber(request.getSourceAccountNumber())
                    .amount(request.getInterestAmount())
                    .transactionReference(transactionReference)
                    .description("Interest payout for savings " + request.getSavingsAccountId())
                    .performedBy("savings-service")
                    .build();

            BalanceOperationResponse balanceResponse = balanceManagementService.credit(creditRequest);
            log.info("[CORE-SAVINGS-WITHDRAW] Credited interest amount {} to account {}", 
                    request.getInterestAmount(), request.getSourceAccountNumber());

            TransactionRecordRequest recordRequest = TransactionRecordRequest.builder()
                    .sourceAccountId(request.getSourceAccountNumber())
                    .destinationAccountId(request.getSourceAccountNumber())
                    .amount(request.getInterestAmount())
                    .transactionType(TransactionType.DEPOSIT)
                    .status(TransactionStatus.COMPLETED)
                    .traceId(transactionReference)
                    .description("Interest payout for savings " + request.getSavingsAccountId())
                    .createdBy("savings-service")
                    .sourceBalanceBefore(balanceResponse.getPreviousBalance())
                    .sourceBalanceAfter(balanceResponse.getNewBalance())
                    .build();

            Transaction transaction = transactionRecordService.recordTransferTransaction(recordRequest);
            transactionId = transaction.getTransactionId();
        }

        if (transactionId == null) {
            transactionId = "SAVINGS-WITHDRAW-" + request.getSavingsAccountId() + "-" + UUID.randomUUID();
        }

        savingsAccount.setLastTransactionId(transactionId);
        savingsAccountRepository.save(savingsAccount);

        log.info("[CORE-SAVINGS-WITHDRAW] Withdrawal processed successfully, transactionId={}", transactionId);
        return transactionId;
    }
}
