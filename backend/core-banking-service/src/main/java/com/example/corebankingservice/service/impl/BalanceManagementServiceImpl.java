package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.request.BalanceOperationRequest;
import com.example.corebankingservice.dto.request.TransferExecutionRequest;
import com.example.corebankingservice.dto.request.TransactionRecordRequest;
import com.example.corebankingservice.dto.response.BalanceOperationResponse;
import com.example.corebankingservice.dto.response.PartnerBankAccountResponse;
import com.example.corebankingservice.dto.response.TransferExecutionResponse;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.BalanceAuditLog;
import com.example.corebankingservice.entity.Transaction;
import com.example.corebankingservice.entity.enums.AccountStatus;
import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.entity.enums.TransactionType;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ErrorCode;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.repository.BalanceAuditLogRepository;
import com.example.corebankingservice.service.BalanceManagementService;
import com.example.corebankingservice.service.PartnerBankService;
import com.example.corebankingservice.service.TransactionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceManagementServiceImpl implements BalanceManagementService {

    private final AccountRepository accountRepository;
    private final BalanceAuditLogRepository auditLogRepository;
    private final TransactionRecordService transactionRecordService;
    private final PartnerBankService partnerBankService;
    private final MessageSource messageSource;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BalanceOperationResponse debit(BalanceOperationRequest request) {
        log.info("Processing DEBIT operation: account={}, amount={}, ref={}", 
                request.getAccountNumber(), request.getAmount(), request.getTransactionReference());

        // Check for idempotency: has this transaction already been processed
        List<BalanceAuditLog> existingAudits = auditLogRepository.findByTransactionReferenceAndOperationType(
                request.getTransactionReference(), "DEBIT");
        if (!existingAudits.isEmpty()) {
            BalanceAuditLog existingAudit = existingAudits.get(0);
            log.warn("DEBIT operation already processed: ref={}, audit_id={}", 
                    request.getTransactionReference(), existingAudit.getAuditId());
            
            // Return the existing operation result (idempotent response)
            return BalanceOperationResponse.builder()
                    .accountNumber(existingAudit.getAccountNumber())
                    .previousBalance(existingAudit.getPreviousBalance())
                    .operationAmount(existingAudit.getOperationAmount())
                    .newBalance(existingAudit.getNewBalance())
                    .availableBalance(existingAudit.getAvailableBalance())
                    .currency(existingAudit.getCurrency())
                    .transactionReference(existingAudit.getTransactionReference())
                    .operationType(existingAudit.getOperationType())
                    .operationTime(existingAudit.getOperationTime())
                    .message(getMessage("success.debit.idempotent"))
                    .build();
        }

        // Lock account for update (SELECT ... FOR UPDATE)
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.account.not.found", request.getAccountNumber())));

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
                .message(getMessage("success.debit.completed"))
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse credit(BalanceOperationRequest request) {
        log.info("Processing CREDIT operation: account={}, amount={}, ref={}", 
                request.getAccountNumber(), request.getAmount(), request.getTransactionReference());

        // Check for idempotency: has this transaction already been processed?
        // IMPORTANT: Must check BOTH transactionReference AND operationType to avoid returning wrong operation result
        List<BalanceAuditLog> existingAudits = auditLogRepository.findByTransactionReferenceAndOperationType(
                request.getTransactionReference(), "CREDIT");
        if (!existingAudits.isEmpty()) {
            BalanceAuditLog existingAudit = existingAudits.get(0);
            log.warn("CREDIT operation already processed: ref={}, audit_id={}", 
                    request.getTransactionReference(), existingAudit.getAuditId());
            
            // Return the existing operation result (idempotent response)
            return BalanceOperationResponse.builder()
                    .accountNumber(existingAudit.getAccountNumber())
                    .previousBalance(existingAudit.getPreviousBalance())
                    .operationAmount(existingAudit.getOperationAmount())
                    .newBalance(existingAudit.getNewBalance())
                    .availableBalance(existingAudit.getAvailableBalance())
                    .currency(existingAudit.getCurrency())
                    .transactionReference(existingAudit.getTransactionReference())
                    .operationType(existingAudit.getOperationType())
                    .operationTime(existingAudit.getOperationTime())
                    .message(getMessage("success.credit.idempotent"))
                    .build();
        }

        // Lock account for update
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.account.not.found", request.getAccountNumber())));

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
                .message(getMessage("success.credit.completed"))
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransferExecutionResponse executeTransfer(TransferExecutionRequest request) {
        log.info("Executing complete transfer in Core Banking: source={}, dest={}, amount={}, bankCode={}, type={}, ref={}", 
                request.getSourceAccountNumber(), 
                request.getDestinationAccountNumber(),
                request.getAmount(),
                request.getDestinationBankCode(),
                request.getTransferType(),
                request.getTransactionReference());

        // Determine if this is an interbank transfer
        boolean isInterbank = request.getDestinationBankCode() != null 
                && !request.getDestinationBankCode().isBlank()
                && !"KIENLONG".equalsIgnoreCase(request.getDestinationBankCode());

        log.info("Transfer type detected: {}", isInterbank ? "INTERBANK" : "INTERNAL");

        // Step 1: Debit from source account
        BalanceOperationRequest debitRequest = BalanceOperationRequest.builder()
                .accountNumber(request.getSourceAccountNumber())
                .amount(request.getAmount())
                .transactionReference(request.getTransactionReference())
                .description("Transfer to " + request.getDestinationAccountNumber())
                .performedBy(request.getPerformedBy())
                .build();

        BalanceOperationResponse debitResponse = debit(debitRequest);
        log.info("Debit successful: account={}, new_balance={}", 
                request.getSourceAccountNumber(), debitResponse.getNewBalance());

        try {
            BalanceOperationResponse creditResponse = null;
            
            if (isInterbank) {
                // Step 2a: Interbank transfer - send to partner bank
                log.info("Executing interbank transfer to bank: {}", request.getDestinationBankCode());
                
                PartnerBankAccountResponse partnerResponse = partnerBankService.sendToPartnerBank(
                        request.getDestinationBankCode(),
                        request.getDestinationAccountNumber(),
                        request.getAmount(),
                        request.getTransactionReference(),
                        request.getDescription()
                );
                
                if (partnerResponse == null || !partnerResponse.getExists()) {
                    throw new BusinessException("Failed to transfer to partner bank: Account not found or inactive");
                }
                
                log.info("Interbank transfer successful to bank: {}", request.getDestinationBankCode());
                
                // For interbank, we don't have destination balance in our system
                // Create a mock response for transaction recording
                creditResponse = BalanceOperationResponse.builder()
                        .accountNumber(request.getDestinationAccountNumber())
                        .previousBalance(BigDecimal.ZERO)
                        .operationAmount(request.getAmount())
                        .newBalance(BigDecimal.ZERO)
                        .availableBalance(BigDecimal.ZERO)
                        .currency(debitResponse.getCurrency())
                        .transactionReference(request.getTransactionReference())
                        .operationType("CREDIT_EXTERNAL")
                        .operationTime(LocalDateTime.now())
                        .message("Transferred to external bank")
                        .build();
                
            } else {
                // Step 2b: Internal transfer - credit to destination account
                BalanceOperationRequest creditRequest = BalanceOperationRequest.builder()
                        .accountNumber(request.getDestinationAccountNumber())
                        .amount(request.getAmount())
                        .transactionReference(request.getTransactionReference())
                        .description("Transfer from " + request.getSourceAccountNumber())
                        .performedBy(request.getPerformedBy())
                        .build();

                creditResponse = credit(creditRequest);
                log.info("Credit successful: account={}, new_balance={}", 
                        request.getDestinationAccountNumber(), creditResponse.getNewBalance());
            }
            // Step 3: Record transaction in Core Banking Transaction table
            TransactionRecordRequest transactionRecordRequest = TransactionRecordRequest.builder()
                    .sourceAccountId(request.getSourceAccountNumber())
                    .destinationAccountId(request.getDestinationAccountNumber())
                    .amount(request.getAmount())
                    .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                    .transactionType(TransactionType.TRANSFER)
                    .status(TransactionStatus.COMPLETED)
                    .traceId(request.getTransactionReference())
                    .description(request.getDescription())
                    .createdBy(request.getPerformedBy())
                    .sourceBalanceBefore(debitResponse.getPreviousBalance())
                    .sourceBalanceAfter(debitResponse.getNewBalance())
                    .destBalanceBefore(creditResponse.getPreviousBalance())
                    .destBalanceAfter(creditResponse.getNewBalance())
                    .build();

            Transaction transaction = transactionRecordService.recordTransferTransaction(transactionRecordRequest);
            log.info("Transaction recorded in Core Banking: transactionId={}, traceId={}", 
                    transaction.getTransactionId(), transaction.getTraceId());

            // Step 4: Build response
            return TransferExecutionResponse.builder()
                    .transactionId(transaction.getTransactionId())
                    .sourceAccountNumber(request.getSourceAccountNumber())
                    .destinationAccountNumber(request.getDestinationAccountNumber())
                    .amount(request.getAmount())
                    .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                    .sourceBalanceBefore(debitResponse.getPreviousBalance())
                    .sourceBalanceAfter(debitResponse.getNewBalance())
                    .sourceAvailableBalance(debitResponse.getAvailableBalance())
                    .destBalanceBefore(creditResponse.getPreviousBalance())
                    .destBalanceAfter(creditResponse.getNewBalance())
                    .destAvailableBalance(creditResponse.getAvailableBalance())
                    .transactionReference(request.getTransactionReference())
                    .currency(debitResponse.getCurrency().name())
                    .executionTime(LocalDateTime.now())
                    .message("Transfer executed successfully and recorded in Core Banking")
                    .build();

        } catch (Exception e) {
            log.error("Transfer execution failed, initiating rollback", e);
            // Rollback: Credit back to source account
            try {
                BalanceOperationRequest rollbackRequest = BalanceOperationRequest.builder()
                        .accountNumber(request.getSourceAccountNumber())
                        .amount(request.getAmount())
                        .transactionReference(request.getTransactionReference() + "-ROLLBACK")
                        .description("Rollback failed transfer")
                        .performedBy("SYSTEM")
                        .build();
                credit(rollbackRequest);
                log.info("Rollback successful");

                // Record failed transaction
                try {
                    TransactionRecordRequest failedTransactionRequest = TransactionRecordRequest.builder()
                            .sourceAccountId(request.getSourceAccountNumber())
                            .destinationAccountId(request.getDestinationAccountNumber())
                            .amount(request.getAmount())
                            .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                            .transactionType(TransactionType.TRANSFER)
                            .status(TransactionStatus.FAILED)
                            .traceId(request.getTransactionReference())
                            .description("FAILED: " + e.getMessage())
                            .createdBy(request.getPerformedBy())
                            .build();
                    transactionRecordService.recordTransferTransaction(failedTransactionRequest);
                } catch (Exception recordException) {
                    log.error("Failed to record failed transaction", recordException);
                }

            } catch (Exception rollbackException) {
                log.error("Rollback failed!", rollbackException);
            }
            throw new BusinessException(ErrorCode.TRANSACTION_FAILED, "Transfer execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse holdAmount(String accountNumber, BigDecimal amount, String transactionReference) {
        log.info("Processing HOLD operation: account={}, amount={}, ref={}", accountNumber, amount, transactionReference);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.account.not.found", accountNumber)));

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
                .description(getMessage("audit.description.amount.held"))
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
                .message(getMessage("success.hold.completed"))
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceOperationResponse releaseHoldAmount(String accountNumber, BigDecimal amount, String transactionReference) {
        log.info("Processing RELEASE_HOLD operation: account={}, amount={}, ref={}", accountNumber, amount, transactionReference);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.account.not.found", accountNumber)));

        if (account.getHoldAmount().compareTo(amount) < 0) {
            throw new BusinessException(getMessage("error.hold.amount.insufficient"));
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
                .description(getMessage("audit.description.hold.released"))
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
                .message(getMessage("success.release.hold.completed"))
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

    private String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }
}
