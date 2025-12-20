package com.example.transactionservice.service.impl;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.account.AccountInfoDTO;
import com.example.commonapi.dto.customer.CustomerBasicInfo;
import com.example.commonapi.dto.digitalotp.DigitalOtpStatusResponse;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationRequest;
import com.example.commonapi.dto.digitalotp.DigitalOtpValidationResponse;
import com.example.transactionservice.client.CoreBankingClient;
import com.example.transactionservice.dto.request.TransferConfirmDTO;
import com.example.transactionservice.dto.request.TransferRequestDTO;
import com.example.transactionservice.dto.request.TransferExecutionRequest;
import com.example.transactionservice.dto.response.BalanceResponse;
import com.example.transactionservice.dto.response.TransferResponseDTO;
import com.example.transactionservice.dto.response.TransferExecutionResponse;
import com.example.transactionservice.dubbo.consumer.AccountServiceClient;
import com.example.transactionservice.dubbo.consumer.CustomerServiceClient;
import com.example.transactionservice.dubbo.consumer.DigitalOtpServiceClient;
import com.example.transactionservice.entity.Transaction;
import com.example.transactionservice.entity.enums.TransactionStatus;
import com.example.transactionservice.entity.enums.TransactionType;
import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.transactionservice.events.producer.TransactionNotificationProducer;
import com.example.transactionservice.exception.*;
import com.example.transactionservice.mapper.TransferMapper;
import com.example.transactionservice.repository.TransactionRepository;
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
    private final CoreBankingClient coreBankingClient;
    private final AccountServiceClient accountServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final DigitalOtpServiceClient digitalOtpServiceClient;
    private final TransferMapper transferMapper;
    private final TransactionNotificationProducer notificationProducer;

    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("100000000000");
    private static final int MAX_DECIMAL_SCALE = 3;

    @Override
    @Transactional
    public TransferResponseDTO initiateTransfer(TransferRequestDTO request) {
        log.info("Initiating transfer from {} to {} amount: {}", 
            request.getSourceAccountNumber(), 
            request.getDestinationAccountNumber(), 
            request.getAmount());

        // Validate raw amount before any downstream call
        validateTransferAmount(request.getAmount());

        // 1. Validate accounts exist and active
        AccountInfoDTO sourceAccountInfo = validateAccounts(
            request.getSourceAccountNumber(),
            request.getDestinationAccountNumber()
        );

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

        // 4. Ensure Digital OTP is available for this customer
        String customerId = extractCustomerId(sourceAccountInfo);
        ensureDigitalOtpReady(customerId);

        return transferMapper.toDigitalOtpResponse(
            transaction,
            "Digital OTP challenge sent. Approve it in your web to continue."
        );
    }

    private void validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidTransactionException("Transfer amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be greater than 0");
        }

        if (amount.scale() > MAX_DECIMAL_SCALE) {
            throw new InvalidTransactionException(
                String.format("Transfer amount precision cannot exceed %d decimal places", MAX_DECIMAL_SCALE)
            );
        }

        if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            throw new InvalidTransactionException(
                String.format("Transfer amount must be at least %s VND", MIN_TRANSFER_AMOUNT.toPlainString())
            );
        }

        if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            throw new InvalidTransactionException(
                String.format("Transfer amount exceeds the maximum limit of %s VND", MAX_TRANSFER_AMOUNT.toPlainString())
            );
        }
    }

    @Override
    @Transactional(noRollbackFor = TransferFailedException.class)
    public TransferResponseDTO confirmTransfer(TransferConfirmDTO confirmDTO) {
        log.info("Confirming transfer: {}", confirmDTO.getTransactionId());

        // 1. Get transaction
        Transaction transaction = transactionRepository.findById(confirmDTO.getTransactionId())
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + confirmDTO.getTransactionId()));

        // 2. Validate transaction status
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Transaction is not in PENDING status: " + transaction.getStatus());
        }

        // 3. Get customer ID from source account
        AccountInfoDTO sourceAccountInfo = accountServiceClient.getAccountInfo(transaction.getSourceAccountId());
        String customerId = extractCustomerId(sourceAccountInfo);

        // 4. Enforce Digital OTP flow
        ensureDigitalOtpReady(customerId);

        if (confirmDTO.getDigitalOtpToken() == null || confirmDTO.getTimestamp() == null) {
            throw new OtpValidationException("Digital OTP token and timestamp are required");
        }

        // Validate Digital OTP
        DigitalOtpValidationRequest validationRequest = DigitalOtpValidationRequest.builder()
            .customerId(customerId)
            .digitalOtpToken(confirmDTO.getDigitalOtpToken())
            .transactionId(transaction.getTransactionId())
            .sourceAccountNumber(transaction.getSourceAccountId())
            .destinationAccountNumber(transaction.getDestinationAccountId())
            .amount(transaction.getAmount())
            .timestamp(confirmDTO.getTimestamp())
            .build();

        DigitalOtpValidationResponse validationResponse = digitalOtpServiceClient.validateDigitalOtp(validationRequest);

        if (!validationResponse.isValid()) {
            log.warn("Digital OTP validation failed for transaction: {}. Error: {}",
                confirmDTO.getTransactionId(), validationResponse.getMessage());
            throw new OtpValidationException(
                validationResponse.getMessage() +
                " (Remaining attempts: " + validationResponse.getRemainingAttempts() + ")"
            );
        }

        log.info("Digital OTP validation successful for transaction: {}", confirmDTO.getTransactionId());

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

        log.info("Transaction cancelled: {}", transactionId);

        return transferMapper.toResponseDTOWithMessage(transaction, "Transaction cancelled");
    }

    /**
     * Execute transfer via Core Banking Service
     * Uses executeTransfer API to ensure transaction is recorded in Core Banking
     */
    private void executeTransfer(Transaction transaction) {
        log.info("Executing transfer via Core Banking Service using executeTransfer API");

        // Build request for complete transfer execution
        TransferExecutionRequest transferRequest = TransferExecutionRequest.builder()
            .sourceAccountNumber(transaction.getSourceAccountId())
            .destinationAccountNumber(transaction.getDestinationAccountId())
            .amount(transaction.getAmount())
            .fee(BigDecimal.ZERO)
            .transactionReference(transaction.getReferenceNumber())
            .description(transaction.getDescription())
            .performedBy(transaction.getCreatedBy())
            .build();

        try {
            // Execute complete transfer (debit + credit + record transaction)
            ApiResponse<TransferExecutionResponse> response = coreBankingClient.executeTransfer(transferRequest);
            
            if (!response.isSuccess()) {
                throw new TransferFailedException("Transfer execution failed: " + response.getMessage());
            }

            TransferExecutionResponse transferResponse = response.getData();
            log.info("Transfer executed successfully. Core Banking TransactionId: {}, TraceId: {}", 
                    transferResponse.getTransactionId(), 
                    transferResponse.getTransactionReference());

        } catch (Exception e) {
            log.error("Transfer execution failed", e);
            throw new TransferFailedException("Transfer execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate accounts exist and active
     */
    private AccountInfoDTO validateAccounts(String sourceAccount, String destinationAccount) {
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
            return sourceAccountInfo;
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

    private String extractCustomerId(AccountInfoDTO accountInfo) {
        if (accountInfo == null || accountInfo.getCustomerId() == null) {
            throw new AccountValidationException(
                ErrorCode.SOURCE_ACCOUNT_NOT_FOUND,
                "Unable to retrieve customer information for source account"
            );
        }
        return accountInfo.getCustomerId();
    }

    private void ensureDigitalOtpReady(String customerId) {
        DigitalOtpStatusResponse status = digitalOtpServiceClient.getDigitalOtpStatus(customerId);
        if (!status.isEnrolled()) {
            throw new OtpValidationException(
                "Digital OTP enrollment is required to initiate or confirm transfers."
            );
        }

        if (status.isLocked()) {
            throw new OtpValidationException(
                "Digital OTP is locked due to too many failed attempts. Please contact support."
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

    /**
     * Gửi notification event qua Kafka khi transfer thành công
     */
    private void sendTransactionNotification(Transaction transaction) {
        try {
            // Lấy thông tin accounts
            AccountInfoDTO sourceAccountInfo = accountServiceClient.getAccountInfo(transaction.getSourceAccountId());
            AccountInfoDTO destAccountInfo = accountServiceClient.getAccountInfo(transaction.getDestinationAccountId());
            
            // Lấy email từ Customer Service qua Dubbo
            String senderEmail = null;
            String receiverEmail = null;
            
            try {
                if (sourceAccountInfo != null && sourceAccountInfo.getCustomerId() != null) {
                    CustomerBasicInfo senderCustomer = customerServiceClient.getCustomerBasicInfo(sourceAccountInfo.getCustomerId());
                    if (senderCustomer != null) {
                        senderEmail = senderCustomer.getEmail();
                    }
                }
                
                if (destAccountInfo != null && destAccountInfo.getCustomerId() != null) {
                    CustomerBasicInfo receiverCustomer = customerServiceClient.getCustomerBasicInfo(destAccountInfo.getCustomerId());
                    if (receiverCustomer != null) {
                        receiverEmail = receiverCustomer.getEmail();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch customer emails via Dubbo, notifications may not be sent: {}", e.getMessage());
            }
            
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
                    .senderCustomerId(sourceAccountInfo != null ? sourceAccountInfo.getCustomerId() : null)
                    .senderName(sourceAccountInfo != null ? sourceAccountInfo.getAccountHolderName() : null)
                    .senderEmail(senderEmail)
                    .senderBankCode(sourceAccountInfo != null ? sourceAccountInfo.getBankCode() : null)
                    
                    // Receiver info
                    .receiverAccountNumber(transaction.getDestinationAccountId())
                    .receiverCustomerId(destAccountInfo != null ? destAccountInfo.getCustomerId() : null)
                    .receiverName(destAccountInfo != null ? destAccountInfo.getAccountHolderName() : null)
                    .receiverEmail(receiverEmail)
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
     * - Chuyển nội bộ (cùng ngân hàng): MIỄN PHÍ
     * - Chuyển liên ngân hàng: MIỄN PHÍ
     * - Chuyển ngoại tệ (không phải VND): 0.5% (tối thiểu 10,000 VND)
     */
    private BigDecimal calculateTransferFee(BigDecimal amount, AccountInfoDTO destAccount) {
        return BigDecimal.ZERO;
    }


}
