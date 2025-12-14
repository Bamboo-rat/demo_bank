package com.example.transactionservice.service.impl;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.account.AccountInfoDTO;
import com.example.transactionservice.client.CoreBankingClient;
import com.example.transactionservice.dto.request.BalanceOperationRequest;
import com.example.transactionservice.dto.request.TransferConfirmDTO;
import com.example.transactionservice.dto.request.TransferRequestDTO;
import com.example.transactionservice.dto.response.BalanceOperationResponse;
import com.example.transactionservice.dto.response.BalanceResponse;
import com.example.transactionservice.dto.response.TransferResponseDTO;
import com.example.transactionservice.dubbo.consumer.AccountServiceClient;
import com.example.transactionservice.entity.Transaction;
import com.example.transactionservice.entity.enums.TransactionStatus;
import com.example.transactionservice.entity.enums.TransactionType;
import com.example.transactionservice.events.TransactionNotificationEvent;
import com.example.transactionservice.events.producer.TransactionNotificationProducer;
import com.example.transactionservice.exception.*;
import com.example.transactionservice.mapper.TransferMapper;
import com.example.transactionservice.repository.TransactionRepository;
import com.example.transactionservice.service.OtpService;
import com.example.transactionservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransactionRepository transactionRepository;
    private final OtpService otpService;
    private final CoreBankingClient coreBankingClient;
    private final AccountServiceClient accountServiceClient;
    private final TransferMapper transferMapper;
    private final TransactionNotificationProducer notificationProducer;

    @Override
    @Transactional
    public TransferResponseDTO initiateTransfer(TransferRequestDTO request) {
        log.info("Initiating transfer from {} to {} amount: {}", 
            request.getSourceAccountNumber(), 
            request.getDestinationAccountNumber(), 
            request.getAmount());

        // 1. Validate accounts exist and active
        validateAccounts(request.getSourceAccountNumber(), request.getDestinationAccountNumber());

        // 2. Check balance before OTP
        validateBalance(request.getSourceAccountNumber(), request.getAmount());

        // 3. Create PENDING transaction
        Transaction transaction = Transaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .sourceAccountId(request.getSourceAccountNumber())
            .destinationAccountId(request.getDestinationAccountNumber())
            .amount(request.getAmount())
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.PENDING)
            .description(request.getDescription())
            .referenceNumber(generateReferenceNumber())
            .createdBy(getAuthenticatedUserId())
            .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", transaction.getTransactionId());

        // 4. Generate and send OTP
        String otp = otpService.generateOtp(transaction.getTransactionId(), request.getPhoneNumber());

        return transferMapper.toOtpResponseDTO(
            transaction,
            maskPhoneNumber(request.getPhoneNumber()),
            5,
            "OTP sent successfully. Please check your phone."
        );
    }

    @Override
    @Transactional
    public TransferResponseDTO confirmTransfer(TransferConfirmDTO confirmDTO) {
        log.info("Confirming transfer: {}", confirmDTO.getTransactionId());

        // 1. Get transaction
        Transaction transaction = transactionRepository.findById(confirmDTO.getTransactionId())
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + confirmDTO.getTransactionId()));

        // 2. Validate transaction status
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Transaction is not in PENDING status: " + transaction.getStatus());
        }

        // 3. Validate OTP
        boolean otpValid = otpService.validateOtp(confirmDTO.getTransactionId(), confirmDTO.getOtp());
        if (!otpValid) {
            log.warn("Invalid OTP for transaction: {}", confirmDTO.getTransactionId());
            throw new OtpValidationException("Invalid or expired OTP");
        }

        // 4. Invalidate OTP after successful validation
        otpService.invalidateOtp(confirmDTO.getTransactionId());

        // 5. Update status to PROCESSING
        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);
        log.info("Transaction status updated to PROCESSING");

        try {
            // 6. Execute transfer via Core Banking Service
            executeTransfer(transaction);

            // 7. Update status to COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("Transfer completed successfully");

            // 8. Send Kafka notification event
            try {
                sendTransactionNotification(transaction);
            } catch (Exception kafkaException) {
                // Log error nhưng không fail transaction
                log.error("Failed to send Kafka notification for transaction: {}", 
                    transaction.getTransactionId(), kafkaException);
            }

            return transferMapper.toResponseDTOWithMessage(transaction, "Transfer completed successfully");

        } catch (Exception e) {
            log.error("Transfer execution failed", e);

            // 8. Update status to FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            // 9. Send failure notification (optional - không block exception)
            try {
                sendFailureNotification(transaction, e.getMessage());
            } catch (Exception notificationException) {
                log.error("Failed to send failure notification", notificationException);
            }

            throw new TransferFailedException("Transfer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public TransferResponseDTO getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        return transferMapper.toResponseDTO(transaction);
    }

    @Override
    public List<TransferResponseDTO> getTransactionHistory(String accountNumber, int page, int size) {
        Page<Transaction> transactions = transactionRepository.findByAccountId(
            accountNumber, 
            PageRequest.of(page, size)
        );

        return transactions.stream()
            .map(transferMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransferResponseDTO cancelTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Can only cancel PENDING transactions");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        // Invalidate OTP if exists
        otpService.invalidateOtp(transactionId);

        log.info("Transaction cancelled: {}", transactionId);

        return transferMapper.toResponseDTOWithMessage(transaction, "Transaction cancelled");
    }

    /**
     * Execute transfer via Core Banking Service
     */
    private void executeTransfer(Transaction transaction) {
        log.info("Executing transfer via Core Banking Service");

        // 1. Debit from source account
        BalanceOperationRequest debitRequest = BalanceOperationRequest.builder()
            .accountNumber(transaction.getSourceAccountId())
            .amount(transaction.getAmount())
            .transactionReference(transaction.getReferenceNumber())
            .description("Transfer to " + transaction.getDestinationAccountId())
            .performedBy(transaction.getCreatedBy())
            .build();

        ApiResponse<BalanceOperationResponse> debitResponse = coreBankingClient.debitBalance(debitRequest);
        
        if (!debitResponse.isSuccess()) {
            throw new TransferFailedException("Debit failed: " + debitResponse.getMessage());
        }
        log.info("Debit successful from account: {}", transaction.getSourceAccountId());

        try {
            // 2. Credit to destination account
            BalanceOperationRequest creditRequest = BalanceOperationRequest.builder()
                .accountNumber(transaction.getDestinationAccountId())
                .amount(transaction.getAmount())
                .transactionReference(transaction.getReferenceNumber())
                .description("Transfer from " + transaction.getSourceAccountId())
                .performedBy(transaction.getCreatedBy())
                .build();

            ApiResponse<BalanceOperationResponse> creditResponse = coreBankingClient.creditBalance(creditRequest);
            
            if (!creditResponse.isSuccess()) {
                // Rollback: Credit back to source account
                log.error("Credit failed, initiating rollback");
                coreBankingClient.creditBalance(debitRequest);
                throw new TransferFailedException("Credit failed: " + creditResponse.getMessage());
            }
            log.info("Credit successful to account: {}", transaction.getDestinationAccountId());
        } catch (Exception e) {
            // Rollback: Credit back to source account
            log.error("Exception during credit, initiating rollback", e);
            try {
                coreBankingClient.creditBalance(debitRequest);
                log.info("Rollback successful");
            } catch (Exception rollbackException) {
                log.error("Rollback failed!", rollbackException);
            }
            throw e;
        }
    }

    /**
     * Validate accounts exist and active
     */
    private void validateAccounts(String sourceAccount, String destinationAccount) {
        if (sourceAccount.equals(destinationAccount)) {
            throw new AccountValidationException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER,
                    "Cannot transfer to the same account"
            );
        }

        try {
            // Validate source account
            AccountInfoDTO sourceAccountInfo = accountServiceClient.getAccountInfo(sourceAccount);
            if (sourceAccountInfo == null) {
                throw new AccountValidationException(
                    ErrorCode.SOURCE_ACCOUNT_NOT_FOUND,
                    "Source account not found: " + sourceAccount
                );
            }
            if (!sourceAccountInfo.getIsActive()) {
                throw new AccountValidationException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE,
                    "Source account is not active"
                );
            }

            // Validate destination account
            AccountInfoDTO destAccountInfo = accountServiceClient.getAccountInfo(destinationAccount);
            if (destAccountInfo == null) {
                throw new AccountValidationException(
                    ErrorCode.DESTINATION_ACCOUNT_NOT_FOUND,
                    "Destination account not found: " + destinationAccount
                );
            }
            if (!destAccountInfo.getIsActive()) {
                throw new AccountValidationException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE,
                    "Destination account is not active"
                );
            }

            log.info("Accounts validated successfully");
        } catch (AccountValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error communicating with Account Service", e);
            throw new ExternalServiceException(
                ErrorCode.ACCOUNT_SERVICE_UNAVAILABLE,
                "Unable to validate accounts. Account service is unavailable: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Validate sufficient balance for transfer
     */
    private void validateBalance(String accountNumber, BigDecimal amount) {
        try {
            ApiResponse<BalanceResponse> balanceResponse = coreBankingClient.getBalance(accountNumber);
            
            if (!balanceResponse.isSuccess() || balanceResponse.getData() == null) {
                throw new InsufficientBalanceException("Unable to fetch balance for account: " + accountNumber);
            }

            BalanceResponse balance = balanceResponse.getData();
            BigDecimal availableBalance = balance.getAvailableBalance();

            if (availableBalance.compareTo(amount) < 0) {
                log.warn("Insufficient balance. Available: {}, Required: {}", availableBalance, amount);
                throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %s, Required: %s", 
                        availableBalance, amount)
                );
            }

            log.info("Balance validated. Available: {}, Required: {}", availableBalance, amount);
        } catch (InsufficientBalanceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking balance", e);
            throw new ExternalServiceException(
                ErrorCode.CORE_BANKING_SERVICE_ERROR,
                "Failed to check balance: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Generate unique reference number
     */
    private String generateReferenceNumber() {
        return "TXN" + System.currentTimeMillis();
    }

    /**
     * Mask phone number for security
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Gửi notification event qua Kafka khi transfer thành công
     */
    private void sendTransactionNotification(Transaction transaction) {
        try {
            // Lấy thông tin accounts
            AccountInfoDTO sourceAccountInfo = accountServiceClient.getAccountInfo(transaction.getSourceAccountId());
            AccountInfoDTO destAccountInfo = accountServiceClient.getAccountInfo(transaction.getDestinationAccountId());
            
            // Lấy balance sau giao dịch
            ApiResponse<BalanceResponse> sourceBalanceResponse = coreBankingClient.getBalance(transaction.getSourceAccountId());
            ApiResponse<BalanceResponse> destBalanceResponse = coreBankingClient.getBalance(transaction.getDestinationAccountId());
            
            // Build notification event
            TransactionNotificationEvent event = TransactionNotificationEvent.builder()
                    .transactionId(transaction.getTransactionId())
                    .transactionReference(transaction.getReferenceNumber())
                    .transactionType(transaction.getType().name())
                    
                    // Sender info
                    .senderAccountNumber(transaction.getSourceAccountId())
                    .senderCustomerId(sourceAccountInfo != null ? sourceAccountInfo.getAccountHolderName() : null)
                    .senderName(sourceAccountInfo != null ? sourceAccountInfo.getAccountHolderName() : null)
                    .senderEmail(null) // TODO: Get from customer service if needed
                    
                    // Receiver info
                    .receiverAccountNumber(transaction.getDestinationAccountId())
                    .receiverCustomerId(destAccountInfo != null ? destAccountInfo.getAccountHolderName() : null)
                    .receiverName(destAccountInfo != null ? destAccountInfo.getAccountHolderName() : null)
                    .receiverEmail(null) // TODO: Get from customer service if needed
                    .receiverBankCode(destAccountInfo != null ? destAccountInfo.getBankCode() : null)
                    .receiverBankName(destAccountInfo != null ? destAccountInfo.getBankName() : null)
                    
                    // Transaction details
                    .amount(transaction.getAmount())
                    .currency("VND")
                    .description(transaction.getDescription())
                    .senderBalanceAfter(sourceBalanceResponse.isSuccess() && sourceBalanceResponse.getData() != null 
                            ? sourceBalanceResponse.getData().getBalance() : null)
                    .receiverBalanceAfter(destBalanceResponse.isSuccess() && destBalanceResponse.getData() != null 
                            ? destBalanceResponse.getData().getBalance() : null)
                    
                    // Metadata
                    .transactionTime(transaction.getTransactionDate() != null ? transaction.getTransactionDate() : LocalDateTime.now())
                    .status("SUCCESS")
                    .fee(calculateTransferFee(transaction.getAmount(), destAccountInfo))
                    .build();
            
            // Send to Kafka asynchronously
            notificationProducer.sendTransactionNotification(event);
            log.info("Transaction notification event sent to Kafka for transaction: {}", transaction.getTransactionId());
            
        } catch (Exception e) {
            log.error("Error creating transaction notification event", e);
            throw e;
        }
    }

    /**
     * Gửi failure notification khi transfer thất bại
     */
    private void sendFailureNotification(Transaction transaction, String errorMessage) {
        try {
            AccountInfoDTO sourceAccountInfo = accountServiceClient.getAccountInfo(transaction.getSourceAccountId());
            
            TransactionNotificationEvent event = TransactionNotificationEvent.builder()
                    .transactionId(transaction.getTransactionId())
                    .transactionReference(transaction.getReferenceNumber())
                    .transactionType(transaction.getType().name())
                    .senderAccountNumber(transaction.getSourceAccountId())
                    .senderName(sourceAccountInfo != null ? sourceAccountInfo.getAccountHolderName() : null)
                    .receiverAccountNumber(transaction.getDestinationAccountId())
                    .amount(transaction.getAmount())
                    .currency("VND")
                    .description(transaction.getDescription() + " - FAILED: " + errorMessage)
                    .transactionTime(LocalDateTime.now())
                    .status("FAILED")
                    .fee(BigDecimal.ZERO)
                    .build();
            
            notificationProducer.sendTransactionNotification(event);
            log.info("Failure notification sent for transaction: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("Error sending failure notification", e);
        }
    }

    /**
     * Lấy authenticated user ID từ SecurityContext
     */
    private String getAuthenticatedUserId() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                // Nếu dùng JWT, có thể lấy từ principal
                if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                    return jwt.getSubject(); // hoặc jwt.getClaim("preferred_username")
                }
                return authentication.getName();
            }
            return "SYSTEM"; // Fallback
        } catch (Exception e) {
            log.warn("Failed to get authenticated user, using SYSTEM", e);
            return "SYSTEM";
        }
    }

    /**
     * Tính phí giao dịch
     * Logic: 
     * - Chuyển nội bộ (cùng ngân hàng): FREE
     * - Chuyển ngoại tệ: 0.5% (min 10,000 VND)
     * - Chuyển liên ngân hàng: 5,500 VND flat fee
     */
    private BigDecimal calculateTransferFee(BigDecimal amount, AccountInfoDTO destAccount) {
        // Chuyển nội bộ - FREE
        if (destAccount != null && (destAccount.getBankCode() == null || destAccount.getBankCode().isBlank())) {
            return BigDecimal.ZERO;
        }
        
        // Chuyển liên ngân hàng - flat fee 5,500 VND
        return new BigDecimal("5500.00");
    }


}
