package com.example.accountservice.service.impl;

import com.example.accountservice.client.CoreBankingServiceClient;
import com.example.accountservice.dto.corebank.CoreBankSavingsCreateRequest;
import com.example.accountservice.dto.corebank.CoreBankSavingsWithdrawRequest;
import com.example.accountservice.dto.corebank.LockFundsRequest;
import com.example.accountservice.dto.corebank.LockFundsResponse;
import com.example.accountservice.dto.savings.*;
import com.example.accountservice.entity.Account;
import com.example.accountservice.mapper.SavingsAccountMapper;
import com.example.accountservice.service.SavingsInterestRateService;
import com.example.accountservice.entity.FixedSavingsAccount;
import com.example.accountservice.entity.enums.AutoRenewType;
import com.example.accountservice.entity.enums.InterestPaymentMethod;
import com.example.accountservice.entity.enums.SavingsAccountStatus;
import com.example.accountservice.entity.enums.SavingsTenor;
import com.example.accountservice.events.model.SavingsOpenedEvent;
import com.example.accountservice.events.model.SavingsWithdrawnEvent;
import com.example.accountservice.events.producer.SavingsEventProducer;
import com.example.accountservice.exception.InvalidSavingsOperationException;
import com.example.accountservice.exception.SavingsAccountNotFoundException;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.FixedSavingsAccountRepository;
import com.example.accountservice.service.FixedSavingsAccountService;
import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.savings.SavingsBasicInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation của Fixed Savings Account Service
 * Xử lý nghiệp vụ tiết kiệm kỳ hạn với đầy đủ validation và logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FixedSavingsAccountServiceImpl implements FixedSavingsAccountService {

    private final FixedSavingsAccountRepository savingsRepository;
    private final AccountRepository accountRepository;
    private final CoreBankingServiceClient coreBankingServiceClient;
    private final SavingsInterestRateService interestRateService;
    private final SavingsEventProducer eventProducer;
    private final SavingsAccountMapper savingsAccountMapper;

    // Constants
    private static final BigDecimal MIN_PRINCIPAL_AMOUNT = new BigDecimal("100000"); // 100k VND
    private static final int DAYS_IN_YEAR = 365;
    private static final BigDecimal PENALTY_RATE = new BigDecimal("0.50"); // Lãi suất rút trước hạn: 0.5%

    @Override
    public SavingsPreviewResponse calculatePreview(SavingsPreviewRequest request) {
        log.info("[SAVINGS-PREVIEW-001] Calculating preview - Amount: {}, Tenor: {}, PaymentMethod: {}", 
                 request.getPrincipalAmount(), request.getTenor(), request.getInterestPaymentMethod());

        // 1. Convert String to Enum
        SavingsTenor tenor = parseEnumSafe(SavingsTenor.class, request.getTenor(), "TENOR");
        InterestPaymentMethod paymentMethod = parseEnumSafe(InterestPaymentMethod.class, 
                request.getInterestPaymentMethod(), "PAYMENT_METHOD");

        // 2. Get interest rate
        int termMonths = tenor.getMonths();
        BigDecimal interestRate = interestRateService.getInterestRate(termMonths, paymentMethod.name());
        
        log.info("[SAVINGS-PREVIEW-002] Interest rate: {}% for {} months, payment method: {}", 
                 interestRate, termMonths, paymentMethod);

        // 3. Calculate dates
        LocalDate startDate = LocalDate.now();
        LocalDate maturityDate = calculateMaturityDate(startDate, termMonths);
        long daysToMaturity = ChronoUnit.DAYS.between(startDate, maturityDate);

        // 4. Calculate estimated interest
        BigDecimal estimatedInterest = calculateInterest(
            request.getPrincipalAmount(),
            interestRate,
            termMonths
        );
        
        BigDecimal totalAmount = request.getPrincipalAmount().add(estimatedInterest);

        log.info("[SAVINGS-PREVIEW-SUCCESS] Preview calculated - Interest: {}, Total: {}, Maturity: {}", 
                 estimatedInterest, totalAmount, maturityDate);

        return SavingsPreviewResponse.builder()
                .principalAmount(request.getPrincipalAmount())
                .interestRate(interestRate)
                .tenor(tenor.name())
                .tenorMonths(termMonths)
                .interestPaymentMethod(paymentMethod.name())
                .estimatedInterest(estimatedInterest)
                .totalAmount(totalAmount)
                .startDate(startDate)
                .maturityDate(maturityDate)
                .daysToMaturity(daysToMaturity)
                .description(String.format("Gửi %s VND trong %d tháng, lãi suất %.2f%%, dự kiến nhận %s VND",
                        request.getPrincipalAmount(), termMonths, interestRate, totalAmount))
                .build();
    }

    @Override
    public List<SavingsProductResponse> getSavingsProducts() {
        log.info("[SAVINGS-PRODUCTS-001] Fetching available savings products");
        
        // Danh sách các sản phẩm tiết kiệm cố định
        List<SavingsProductResponse> products = new ArrayList<>();
        
        // Sản phẩm 3 tháng
        products.add(SavingsProductResponse.builder()
                .productCode("FIXED_3M")
                .productName("Tiết kiệm kỳ hạn 3 tháng")
                .description("Gửi tiết kiệm có kỳ hạn 3 tháng với lãi suất cố định")
                .minAmount(new BigDecimal("1000000"))
                .maxAmount(BigDecimal.ZERO)
                .termMonths(3)
                .interestRate(new BigDecimal("4.5"))
                .earlyWithdrawalPenalty(new BigDecimal("1.5"))
                .build());
        
        // Sản phẩm 6 tháng
        products.add(SavingsProductResponse.builder()
                .productCode("FIXED_6M")
                .productName("Tiết kiệm kỳ hạn 6 tháng")
                .description("Gửi tiết kiệm có kỳ hạn 6 tháng với lãi suất cố định")
                .minAmount(new BigDecimal("1000000"))
                .maxAmount(BigDecimal.ZERO)
                .termMonths(6)
                .interestRate(new BigDecimal("5.0"))
                .earlyWithdrawalPenalty(new BigDecimal("2.0"))
                .build());
        
        // Sản phẩm 9 tháng
        products.add(SavingsProductResponse.builder()
                .productCode("FIXED_9M")
                .productName("Tiết kiệm kỳ hạn 9 tháng")
                .description("Gửi tiết kiệm có kỳ hạn 9 tháng với lãi suất cố định")
                .minAmount(new BigDecimal("1000000"))
                .maxAmount(BigDecimal.ZERO)
                .termMonths(9)
                .interestRate(new BigDecimal("5.3"))
                .earlyWithdrawalPenalty(new BigDecimal("2.3"))
                .build());
        
        // Sản phẩm 12 tháng
        products.add(SavingsProductResponse.builder()
                .productCode("FIXED_12M")
                .productName("Tiết kiệm kỳ hạn 12 tháng")
                .description("Gửi tiết kiệm có kỳ hạn 12 tháng với lãi suất cố định")
                .minAmount(new BigDecimal("1000000"))
                .maxAmount(BigDecimal.ZERO)
                .termMonths(12)
                .interestRate(new BigDecimal("5.5"))
                .earlyWithdrawalPenalty(new BigDecimal("2.5"))
                .build());
        
        // Sản phẩm 18 tháng
        products.add(SavingsProductResponse.builder()
                .productCode("FIXED_18M")
                .productName("Tiết kiệm kỳ hạn 18 tháng")
                .description("Gửi tiết kiệm có kỳ hạn 18 tháng với lãi suất cố định")
                .minAmount(new BigDecimal("5000000"))
                .maxAmount(BigDecimal.ZERO)
                .termMonths(18)
                .interestRate(new BigDecimal("5.8"))
                .earlyWithdrawalPenalty(new BigDecimal("3.0"))
                .build());
        
        // Sản phẩm 24 tháng
        products.add(SavingsProductResponse.builder()
                .productCode("FIXED_24M")
                .productName("Tiết kiệm kỳ hạn 24 tháng")
                .description("Gửi tiết kiệm có kỳ hạn 24 tháng với lãi suất cố định cao nhất")
                .minAmount(new BigDecimal("10000000"))
                .maxAmount(BigDecimal.ZERO)
                .termMonths(24)
                .interestRate(new BigDecimal("6.0"))
                .earlyWithdrawalPenalty(new BigDecimal("3.5"))
                .build());
        
        log.info("[SAVINGS-PRODUCTS-SUCCESS] Fetched {} products", products.size());
        return products;
    }

    @Override
    @Transactional
    public SavingsAccountResponse openSavingsAccount(OpenSavingsRequest request, String customerId) {
        log.info("[SAVINGS-OPEN-001] Opening savings account for customerId={}, sourceAccount={}, amount={}, tenor={}", 
                 customerId, request.getSourceAccountNumber(), request.getPrincipalAmount(), request.getTenor());

        // 1. Convert String to Enum
        SavingsTenor tenor = parseEnumSafe(SavingsTenor.class, request.getTenor(), "TENOR");
        InterestPaymentMethod paymentMethod = parseEnumSafe(InterestPaymentMethod.class, request.getInterestPaymentMethod(), "PAYMENT_METHOD");
        AutoRenewType autoRenew = parseEnumSafe(AutoRenewType.class, request.getAutoRenewType(), "AUTO_RENEW");

        // 2. Validate principal amount
        if (request.getPrincipalAmount().compareTo(MIN_PRINCIPAL_AMOUNT) < 0) {
            log.error("[SAVINGS-OPEN-ERR-001] Principal amount {} is below minimum {}", 
                     request.getPrincipalAmount(), MIN_PRINCIPAL_AMOUNT);
            throw new InvalidSavingsOperationException(
                InvalidSavingsOperationException.MINIMUM_AMOUNT_NOT_MET,
                String.format("Minimum principal amount is %s VND", MIN_PRINCIPAL_AMOUNT)
            );
        }

        // 3. Verify source account exists and belongs to customer
        Account sourceAccount = accountRepository
                .findByAccountNumberAndCustomerId(request.getSourceAccountNumber(), customerId)
                .orElse(null);

        if (sourceAccount == null) {
            log.error("[SAVINGS-OPEN-ERR-002] Source account {} not found or does not belong to customer {}", 
                     request.getSourceAccountNumber(), customerId);
            throw new InvalidSavingsOperationException(
                "SAVINGS_INVALID_SOURCE_ACCOUNT",
                "Source account not found or unauthorized"
            );
        }

        // 3. Check balance from Core Banking
        try {
            ApiResponse<?> balanceResponse = coreBankingServiceClient.getBalance(request.getSourceAccountNumber());
            BigDecimal availableBalance = extractBalance(balanceResponse);
            
            if (availableBalance.compareTo(request.getPrincipalAmount()) < 0) {
                log.error("[SAVINGS-OPEN-ERR-003] Insufficient balance. Available: {}, Required: {}", 
                         availableBalance, request.getPrincipalAmount());
                throw new InvalidSavingsOperationException(
                    InvalidSavingsOperationException.INSUFFICIENT_BALANCE,
                    String.format("Insufficient balance. Available: %s, Required: %s", 
                                availableBalance, request.getPrincipalAmount())
                );
            }
            
            log.info("[SAVINGS-OPEN-002] Balance check passed. Available: {}", availableBalance);
        } catch (Exception e) {
            log.error("[SAVINGS-OPEN-ERR-004] Failed to check balance from Core Banking", e);
            throw new InvalidSavingsOperationException(
                "SAVINGS_BALANCE_CHECK_FAILED",
                "Failed to verify account balance: " + e.getMessage()
            );
        }

        // 4. Snapshot interest rate
        int termMonths = tenor.getMonths();
        BigDecimal interestRate = interestRateService.getInterestRate(
            termMonths, 
            paymentMethod.name()
        );
        
        log.info("[SAVINGS-OPEN-003] Snapshot interest rate: {}% for tenor {} months, payment method {}", 
                 interestRate, termMonths, paymentMethod);

        // 5. Calculate dates
        LocalDate startDate = LocalDate.now();
        LocalDate maturityDate = calculateMaturityDate(startDate, termMonths);
        
        log.info("[SAVINGS-OPEN-004] Calculated dates - Start: {}, Maturity: {}", startDate, maturityDate);

        // 6. Validate beneficiary account if provided
        String beneficiaryAccount = request.getBeneficiaryAccountNumber();
        if (beneficiaryAccount != null && !beneficiaryAccount.isBlank()) {
            boolean beneficiaryExists = accountRepository.findByAccountNumberAndCustomerId(
                beneficiaryAccount, customerId
            ).isPresent();
            
            if (!beneficiaryExists) {
                log.error("[SAVINGS-OPEN-ERR-005] Beneficiary account {} not found or unauthorized", beneficiaryAccount);
                throw new InvalidSavingsOperationException(
                    "SAVINGS_INVALID_BENEFICIARY_ACCOUNT",
                    "Beneficiary account not found or unauthorized"
                );
            }
            log.info("[SAVINGS-OPEN-005] Validated beneficiary account: {}", beneficiaryAccount);
        } else {
            // Default to source account
            beneficiaryAccount = request.getSourceAccountNumber();
            log.info("[SAVINGS-OPEN-005] Using source account as beneficiary: {}", beneficiaryAccount);
        }

        // 7. Create savings account entity (chưa có số sổ - sẽ nhận từ core-banking)
        FixedSavingsAccount savingsAccount = FixedSavingsAccount.builder()
                .savingsAccountId(UUID.randomUUID().toString())
                .customerId(customerId)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .beneficiaryAccountNumber(beneficiaryAccount)
                .principalAmount(request.getPrincipalAmount())
                .interestRate(interestRate)
                .tenor(tenor)
                .interestPaymentMethod(paymentMethod)
                .autoRenewType(autoRenew)
                .status(SavingsAccountStatus.ACTIVE)
                .startDate(startDate)
                .maturityDate(maturityDate)
                .description(request.getDescription())
                .totalPaidInterest(BigDecimal.ZERO)
                .build();

        // 8. Save to database (chưa có số sổ)
        savingsAccount = savingsRepository.save(savingsAccount);
        log.info("[SAVINGS-OPEN-006] Created savings account with ID: {}", savingsAccount.getSavingsAccountId());

        // 10. Lock money in Core Banking
        LockFundsResponse lockResponse;
        try {
            LockFundsRequest lockRequest =
                LockFundsRequest.builder()
                    .accountNumber(request.getSourceAccountNumber())
                    .amount(request.getPrincipalAmount())
                    .lockType("SAVINGS")
                    .referenceId(savingsAccount.getSavingsAccountId())
                    .description("Lock tiền cho sổ tiết kiệm kỳ hạn " + tenor.getLabel())
                    .build();
            
            lockResponse = coreBankingServiceClient.lockFunds(lockRequest);
            
            log.info("[SAVINGS-OPEN-007] Successfully locked funds - Lock ID: {}, Available: {}", 
                     lockResponse.getLockId(), lockResponse.getAvailableBalance());
                     
        } catch (Exception e) {
            log.error("[SAVINGS-OPEN-ERR-006] Failed to lock funds in Core Banking", e);
            // Rollback: Delete savings account
            savingsRepository.delete(savingsAccount);
            throw new InvalidSavingsOperationException(
                "SAVINGS_LOCK_FAILED",
                "Failed to lock funds in core banking: " + e.getMessage()
            );
        }

        // 10. Persist savings account in core-banking và nhận số sổ tiết kiệm
        String savingsAccountNumber;
        try {
            CoreBankSavingsCreateRequest createRequest = CoreBankSavingsCreateRequest.builder()
                    .savingsAccountId(savingsAccount.getSavingsAccountId())
                    .customerId(customerId)
                    .sourceAccountNumber(request.getSourceAccountNumber())
                    .principalAmount(request.getPrincipalAmount())
                    .interestRate(interestRate)
                    .tenor(tenor.name())
                    .tenorMonths(termMonths)
                    .interestPaymentMethod(paymentMethod.name())
                    .autoRenewType(autoRenew.name())
                    .startDate(startDate)
                    .maturityDate(maturityDate)
                    .description(request.getDescription())
                    .build();

            ApiResponse<String> createResponse = coreBankingServiceClient.createSavingsAccount(createRequest);
            savingsAccountNumber = createResponse.getData();
            
            // Cập nhật số sổ vào entity
            savingsAccount.setSavingsAccountNumber(savingsAccountNumber);
            savingsAccount = savingsRepository.save(savingsAccount);
            
            log.info("[SAVINGS-OPEN-008] Core banking created savings with number: {}", savingsAccountNumber);
        } catch (Exception e) {
            log.error("[SAVINGS-OPEN-ERR-007] Failed to persist savings in Core Banking", e);
            try {
                coreBankingServiceClient.unlockFundsByReference(
                        savingsAccount.getSavingsAccountId(),
                        "Rollback savings creation due to core banking failure"
                );
            } catch (Exception unlockEx) {
                log.warn("[SAVINGS-OPEN-ERR-007A] Failed to unlock funds during rollback", unlockEx);
            }
            savingsRepository.delete(savingsAccount);
            throw new InvalidSavingsOperationException(
                    "SAVINGS_CORE_BANK_CREATE_FAILED",
                    "Failed to persist savings account in core banking: " + e.getMessage()
            );
        }

        // 11. Publish Kafka event
        publishSavingsOpenedEvent(savingsAccount);

        // 12. Build response
        SavingsAccountResponse response = buildSavingsAccountResponse(savingsAccount);
        
        log.info("[SAVINGS-OPEN-SUCCESS] Successfully opened savings account ID: {}", savingsAccount.getSavingsAccountId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsAccountResponse getSavingsAccountById(String savingsAccountId, String customerId) {
        log.info("[SAVINGS-GET-001] Fetching savings account ID: {} for customer: {}", savingsAccountId, customerId);

        FixedSavingsAccount savingsAccount = savingsRepository
                .findByCustomerIdAndSavingsAccountId(customerId, savingsAccountId)
                .orElseThrow(() -> {
                    log.error("[SAVINGS-GET-ERR-001] Savings account not found or unauthorized: {}", savingsAccountId);
                    return new SavingsAccountNotFoundException(savingsAccountId);
                });

        log.info("[SAVINGS-GET-SUCCESS] Found savings account: {}", savingsAccountId);
        return buildSavingsAccountResponse(savingsAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsAccountResponse> getCustomerSavingsAccounts(String customerId) {
        log.info("[SAVINGS-LIST-001] Fetching all savings accounts for customer: {}", customerId);

        List<FixedSavingsAccount> accounts = savingsRepository.findByCustomerId(customerId);
        log.info("[SAVINGS-LIST-SUCCESS] Found {} savings accounts", accounts.size());

        return accounts.stream()
                .map(this::buildSavingsAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PrematureWithdrawResponse prematureWithdraw(String savingsAccountId, String customerId) {
        log.info("[SAVINGS-WITHDRAW-001] Processing premature withdrawal for ID: {}, customer: {}", 
                 savingsAccountId, customerId);

        // 1. Find savings account
        FixedSavingsAccount savingsAccount = savingsRepository
                .findByCustomerIdAndSavingsAccountId(customerId, savingsAccountId)
                .orElseThrow(() -> {
                    log.error("[SAVINGS-WITHDRAW-ERR-001] Savings account not found: {}", savingsAccountId);
                    return new SavingsAccountNotFoundException(savingsAccountId);
                });

        // 2. Validate can withdraw
        if (!savingsAccount.canPrematureWithdraw()) {
            log.error("[SAVINGS-WITHDRAW-ERR-002] Cannot withdraw - Status: {}, Matured: {}", 
                     savingsAccount.getStatus(), savingsAccount.isMatured());
            throw new InvalidSavingsOperationException(
                InvalidSavingsOperationException.PREMATURE_WITHDRAWAL_NOT_ALLOWED,
                "Premature withdrawal not allowed for this account"
            );
        }

        // 3. Calculate penalty interest
        long daysHeld = ChronoUnit.DAYS.between(savingsAccount.getStartDate(), LocalDate.now());
        BigDecimal penaltyInterest = calculatePenaltyInterest(
            savingsAccount.getPrincipalAmount(), 
            daysHeld
        );
        
        log.info("[SAVINGS-WITHDRAW-002] Calculated penalty interest: {} for {} days", penaltyInterest, daysHeld);

        // 4. Total amount to return
        BigDecimal totalAmount = savingsAccount.getPrincipalAmount().add(penaltyInterest);
        
        // 5. Calculate lost interest (estimated - penalty)
        BigDecimal estimatedFullTermInterest = calculateInterest(
            savingsAccount.getPrincipalAmount(),
            savingsAccount.getInterestRate(),
            savingsAccount.getTenor().getMonths()
        );
        BigDecimal penaltyAmount = estimatedFullTermInterest.subtract(penaltyInterest);
        
        log.info("[SAVINGS-WITHDRAW-003] Principal: {}, Interest: {}, Penalty: {}, Total: {}", 
                 savingsAccount.getPrincipalAmount(), penaltyInterest, penaltyAmount, totalAmount);

        // 6. Update account status
        savingsAccount.setStatus(SavingsAccountStatus.CANCELLED);
        savingsAccount.setWithdrawnAt(LocalDateTime.now());
        savingsAccount.setPenaltyAmount(penaltyAmount);
        savingsRepository.save(savingsAccount);
        
        log.info("[SAVINGS-WITHDRAW-004] Updated account status to CANCELLED");

        // 7. Process withdrawal in Core Banking and get transaction ID
        String transactionId;
        try {
            CoreBankSavingsWithdrawRequest withdrawRequest =
                CoreBankSavingsWithdrawRequest.builder()
                    .savingsAccountId(savingsAccountId)
                    .sourceAccountNumber(savingsAccount.getSourceAccountNumber())
                    .principalAmount(savingsAccount.getPrincipalAmount())
                    .interestAmount(penaltyInterest)
                    .penaltyAmount(penaltyAmount)
                    .totalAmount(totalAmount)
                    .withdrawnAt(LocalDateTime.now())
                    .reason("Premature withdrawal")
                    .build();
            
            ApiResponse<String> withdrawResponse = coreBankingServiceClient.withdrawSavings(withdrawRequest);
            transactionId = withdrawResponse.getData();
            
            log.info("[SAVINGS-WITHDRAW-005] Successfully processed withdrawal in Core Banking - Transaction ID: {}", transactionId);
        } catch (Exception e) {
            log.error("[SAVINGS-WITHDRAW-ERR-003] Failed to process withdrawal in Core Banking", e);
            throw new InvalidSavingsOperationException(
                "SAVINGS_WITHDRAW_FAILED",
                "Failed to process withdrawal transaction: " + e.getMessage()
            );
        }

        // 8. Build response with actual transaction ID
        PrematureWithdrawResponse response = PrematureWithdrawResponse.builder()
                .principalAmount(savingsAccount.getPrincipalAmount())
                .interestAmount(penaltyInterest)
                .penaltyAmount(penaltyAmount)
                .totalAmount(totalAmount)
                .transactionId(transactionId)
                .build();

        // 9. Unlock funds in Core Banking
        try {
            coreBankingServiceClient.unlockFundsByReference(
                savingsAccountId, 
                "Withdrawal completed - Transaction ID: " + transactionId
            );
            log.info("[SAVINGS-WITHDRAW-006] Successfully unlocked funds in Core Banking");
        } catch (Exception e) {
            log.error("[SAVINGS-WITHDRAW-ERR-004] Failed to unlock funds in Core Banking", e);
            // Don't rollback - withdrawal already completed, log for manual resolution
        }

        // 10. Publish Kafka event
        publishSavingsWithdrawnEvent(savingsAccount, response);

        log.info("[SAVINGS-WITHDRAW-SUCCESS] Premature withdrawal completed for ID: {}", savingsAccountId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsBasicInfo getSavingsBasicInfo(String savingsAccountId) {
        log.info("[SAVINGS-DUBBO-001] Fetching basic info for savings account: {}", savingsAccountId);

        FixedSavingsAccount account = savingsRepository.findById(savingsAccountId)
                .orElseThrow(() -> new SavingsAccountNotFoundException(savingsAccountId));

        return buildBasicInfo(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsBasicInfo> getCustomerSavingsBasicInfo(String customerId) {
        log.info("[SAVINGS-DUBBO-002] Fetching basic info for all savings of customer: {}", customerId);

        List<FixedSavingsAccount> accounts = savingsRepository.findByCustomerId(customerId);
        
        return accounts.stream()
                .map(this::buildBasicInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSavingsAccountActive(String savingsAccountId) {
        log.info("[SAVINGS-DUBBO-003] Checking if savings account is active: {}", savingsAccountId);

        return savingsRepository.findById(savingsAccountId)
                .map(account -> account.getStatus() == SavingsAccountStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getTotalSavingsBalance(String customerId) {
        log.info("[SAVINGS-DUBBO-004] Calculating total savings balance for customer: {}", customerId);

        String total = savingsRepository.getTotalSavingsBalance(customerId);
        log.info("[SAVINGS-DUBBO-004-RESULT] Total savings balance: {}", total);
        
        return total;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Tính ngày đáo hạn
     */
    private LocalDate calculateMaturityDate(LocalDate startDate, int months) {
        if (months == 0) {
            // Không kỳ hạn - không có ngày đáo hạn cụ thể
            return startDate.plusYears(100); // Set far future date
        }
        return startDate.plusMonths(months);
    }

    /**
     * Tính lãi suất đầy đủ kỳ hạn (dùng công thức lãi đơn)
     */
    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal annualRate, int months) {
        // Công thức: Interest = Principal * (Rate/100) * (Months/12)
        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal termFraction = BigDecimal.valueOf(months).divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        
        return principal.multiply(rate).multiply(termFraction).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính lãi suất phạt (rút trước hạn)
     * Áp dụng lãi suất không kỳ hạn (0.5%/năm)
     */
    private BigDecimal calculatePenaltyInterest(BigDecimal principal, long daysHeld) {
        // Công thức: Interest = Principal * (PenaltyRate/100) * (Days/365)
        BigDecimal rate = PENALTY_RATE.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal daysFraction = BigDecimal.valueOf(daysHeld).divide(BigDecimal.valueOf(DAYS_IN_YEAR), 6, RoundingMode.HALF_UP);
        
        return principal.multiply(rate).multiply(daysFraction).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Extract balance từ Core Banking response
     */
    private BigDecimal extractBalance(ApiResponse<?> response) {
        if (response == null || response.getData() == null) {
            log.error("[SAVINGS-BALANCE-ERR] Core Banking response is null");
            throw new InvalidSavingsOperationException(
                "SAVINGS_BALANCE_UNAVAILABLE",
                "Unable to retrieve balance from Core Banking"
            );
        }
        
        try {
            // Cast to BalanceResponse and extract availableBalance
            com.example.accountservice.dto.response.BalanceResponse balanceResponse = 
                (com.example.accountservice.dto.response.BalanceResponse) response.getData();
            
            return balanceResponse.getAvailableBalance();
        } catch (ClassCastException e) {
            log.error("[SAVINGS-BALANCE-ERR] Failed to parse Core Banking response", e);
            throw new InvalidSavingsOperationException(
                "SAVINGS_BALANCE_PARSE_ERROR",
                "Invalid response format from Core Banking"
            );
        }
    }

    private SavingsAccountResponse buildSavingsAccountResponse(FixedSavingsAccount account) {
        // Calculate estimated interest
        BigDecimal estimatedInterest = calculateInterest(
            account.getPrincipalAmount(),
            account.getInterestRate(),
            account.getTenor().getMonths()
        );
        
        BigDecimal totalAmount = account.getPrincipalAmount().add(estimatedInterest);

        return SavingsAccountResponse.builder()
                .savingsAccountId(account.getSavingsAccountId())
                .savingsAccountNumber(account.getSavingsAccountNumber())
                .customerId(account.getCustomerId())
                .sourceAccountNumber(account.getSourceAccountNumber())
                .beneficiaryAccountNumber(account.getBeneficiaryAccountNumber())
                .principalAmount(account.getPrincipalAmount())
                .interestRate(account.getInterestRate())
                .tenor(account.getTenor().name())
                .interestPaymentMethod(account.getInterestPaymentMethod().name())
                .autoRenewType(account.getAutoRenewType().name())
                .status(account.getStatus().name())
                .startDate(account.getStartDate().atStartOfDay())
                .maturityDate(account.getMaturityDate().atStartOfDay())
                .description(account.getDescription())
                .estimatedInterest(estimatedInterest)
                .totalAmount(totalAmount)
                .daysUntilMaturity(account.getDaysUntilMaturity())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private SavingsBasicInfo buildBasicInfo(FixedSavingsAccount account) {
        return savingsAccountMapper.toBasicInfo(account);
    }

    /**
     * Publish Kafka event khi mở sổ tiết kiệm
     */
    private void publishSavingsOpenedEvent(FixedSavingsAccount account) {
        try {
            SavingsOpenedEvent event = SavingsOpenedEvent.builder()
                    .savingsAccountId(account.getSavingsAccountId())
                    .customerId(account.getCustomerId())
                    .sourceAccountNumber(account.getSourceAccountNumber())
                    .principalAmount(account.getPrincipalAmount())
                    .interestRate(account.getInterestRate())
                    .tenor(account.getTenor().name())
                    .startDate(account.getStartDate())
                    .maturityDate(account.getMaturityDate())
                    .timestamp(LocalDateTime.now())
                    .message(String.format("Mở sổ tiết kiệm thành công. Số tiền: %s VND, Kỳ hạn: %d tháng, Lãi suất: %s%%",
                                         account.getPrincipalAmount(), 
                                         account.getTenor().getMonths(),
                                         account.getInterestRate()))
                    .build();

            eventProducer.publishSavingsOpenedEvent(event);
            log.info("[SAVINGS-KAFKA] Published SavingsOpenedEvent for account: {}", account.getSavingsAccountId());
        } catch (Exception e) {
            log.error("[SAVINGS-KAFKA-ERROR] Failed to publish SavingsOpenedEvent", e);
            // Don't throw exception - event publishing failure shouldn't block main flow
        }
    }

    /**
     * Publish Kafka event khi rút trước hạn
     */
    private void publishSavingsWithdrawnEvent(FixedSavingsAccount account, PrematureWithdrawResponse response) {
        try {
            SavingsWithdrawnEvent event = SavingsWithdrawnEvent.builder()
                    .savingsAccountId(account.getSavingsAccountId())
                    .customerId(account.getCustomerId())
                    .sourceAccountNumber(account.getSourceAccountNumber())
                    .principalAmount(response.getPrincipalAmount())
                    .interestAmount(response.getInterestAmount())
                    .penaltyAmount(response.getPenaltyAmount())
                    .totalAmount(response.getTotalAmount())
                    .withdrawnAt(LocalDateTime.now())
                    .message(String.format("Rút tiền trước hạn. Tổng nhận: %s VND (Gốc: %s, Lãi: %s, Phạt: %s)",
                                         response.getTotalAmount(),
                                         response.getPrincipalAmount(),
                                         response.getInterestAmount(),
                                         response.getPenaltyAmount()))
                    .build();

            eventProducer.publishSavingsWithdrawnEvent(event);
            log.info("[SAVINGS-KAFKA] Published SavingsWithdrawnEvent for account: {}", account.getSavingsAccountId());
        } catch (Exception e) {
            log.error("[SAVINGS-KAFKA-ERROR] Failed to publish SavingsWithdrawnEvent", e);
        }
    }

    /**
     * Parse enum an toàn từ String
     */
    private <T extends Enum<T>> T parseEnumSafe(Class<T> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("[SAVINGS-PARSE-ERROR] Invalid {} value: {}", fieldName, value);
            throw new InvalidSavingsOperationException(
                "SAVINGS_INVALID_" + fieldName,
                String.format("Invalid %s: %s", fieldName, value)
            );
        }
    }
}
