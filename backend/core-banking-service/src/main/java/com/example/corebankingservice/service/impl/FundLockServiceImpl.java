package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.LockFundsRequest;
import com.example.corebankingservice.dto.LockFundsResponse;
import com.example.corebankingservice.dto.UnlockFundsRequest;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.FundLock;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ErrorCode;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.repository.FundLockRepository;
import com.example.corebankingservice.service.FundLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation của FundLockService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundLockServiceImpl implements FundLockService {
    
    private final FundLockRepository fundLockRepository;
    private final AccountRepository accountRepository;
    
    @Override
    @Transactional
    public LockFundsResponse lockFunds(LockFundsRequest request) {
        log.info("[FUND-LOCK] Locking funds - Account: {}, Amount: {}, Type: {}, Ref: {}", 
                 request.getAccountNumber(), request.getAmount(), request.getLockType(), request.getReferenceId());
        
        // 1. Validate account exists
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> {
                    log.error("[FUND-LOCK-ERR-001] Account not found: {}", request.getAccountNumber());
                    return new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, 
                        "Account not found: " + request.getAccountNumber());
                });
        
        // 2. Calculate total locked amount
        BigDecimal totalLockedAmount = fundLockRepository.getTotalLockedAmount(request.getAccountNumber());
        
        // 3. Calculate available balance (balance - locked)
        BigDecimal availableBalance = account.getBalance().subtract(totalLockedAmount);
        
        log.info("[FUND-LOCK-002] Account balance: {}, Already locked: {}, Available: {}", 
                 account.getBalance(), totalLockedAmount, availableBalance);
        
        // 4. Validate sufficient balance
        if (availableBalance.compareTo(request.getAmount()) < 0) {
            log.error("[FUND-LOCK-ERR-002] Insufficient balance. Available: {}, Required: {}", 
                     availableBalance, request.getAmount());
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                String.format("Insufficient balance. Available: %s, Required: %s", 
                            availableBalance, request.getAmount()));
        }
        
        // 5. Create fund lock record
        FundLock fundLock = FundLock.builder()
                .accountNumber(request.getAccountNumber())
                .lockedAmount(request.getAmount())
                .lockType(request.getLockType())
                .referenceId(request.getReferenceId())
                .description(request.getDescription())
                .status("LOCKED")
                .build();
        
        fundLock = fundLockRepository.save(fundLock);
        
        log.info("[FUND-LOCK-003] Successfully locked funds - Lock ID: {}", fundLock.getLockId());
        
        // 6. Calculate new available balance after lock
        BigDecimal newAvailableBalance = availableBalance.subtract(request.getAmount());
        
        return LockFundsResponse.builder()
                .lockId(fundLock.getLockId())
                .accountNumber(fundLock.getAccountNumber())
                .lockedAmount(fundLock.getLockedAmount())
                .availableBalance(newAvailableBalance)
                .lockType(fundLock.getLockType())
                .referenceId(fundLock.getReferenceId())
                .lockedAt(fundLock.getLockedAt())
                .status(fundLock.getStatus())
                .build();
    }
    
    @Override
    @Transactional
    public LockFundsResponse unlockFunds(UnlockFundsRequest request) {
        log.info("[FUND-UNLOCK-001] Unlocking funds - Lock ID: {}, Reason: {}", 
                 request.getLockId(), request.getReason());
        
        FundLock fundLock = fundLockRepository.findById(request.getLockId())
                .orElseThrow(() -> {
                    log.error("[FUND-UNLOCK-ERR-001] Fund lock not found: {}", request.getLockId());
                    return new IllegalArgumentException("Fund lock not found: " + request.getLockId());
                });
        
        if (!"LOCKED".equals(fundLock.getStatus())) {
            log.warn("[FUND-UNLOCK-WARN-001] Fund lock already released: {}", request.getLockId());
            throw new IllegalStateException("Fund lock already released");
        }
        
        // Update lock status
        fundLock.setStatus("RELEASED");
        fundLock.setReleasedAt(LocalDateTime.now());
        fundLock.setReleaseReason(request.getReason());
        fundLock = fundLockRepository.save(fundLock);
        
        log.info("[FUND-UNLOCK-002] Successfully unlocked funds - Lock ID: {}", fundLock.getLockId());
        
        return LockFundsResponse.builder()
                .lockId(fundLock.getLockId())
                .accountNumber(fundLock.getAccountNumber())
                .lockedAmount(fundLock.getLockedAmount())
                .lockType(fundLock.getLockType())
                .referenceId(fundLock.getReferenceId())
                .lockedAt(fundLock.getLockedAt())
                .status(fundLock.getStatus())
                .build();
    }
    
    @Override
    @Transactional
    public LockFundsResponse unlockFundsByReference(String referenceId, String reason) {
        log.info("[FUND-UNLOCK-REF-001] Unlocking funds by reference - Ref: {}, Reason: {}", 
                 referenceId, reason);
        
        FundLock fundLock = fundLockRepository.findByReferenceIdAndStatus(referenceId, "LOCKED")
                .orElseThrow(() -> {
                    log.error("[FUND-UNLOCK-REF-ERR-001] Active fund lock not found for reference: {}", referenceId);
                    return new IllegalArgumentException("Active fund lock not found for reference: " + referenceId);
                });
        
        UnlockFundsRequest request = UnlockFundsRequest.builder()
                .lockId(fundLock.getLockId())
                .reason(reason)
                .build();
        
        return unlockFunds(request);
    }
}
