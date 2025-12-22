package com.example.corebankingservice.controller;

import com.example.corebankingservice.dto.LockFundsRequest;
import com.example.corebankingservice.dto.LockFundsResponse;
import com.example.corebankingservice.dto.UnlockFundsRequest;
import com.example.corebankingservice.service.FundLockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core-banking/fund-locks")
@RequiredArgsConstructor
@Slf4j
public class FundLockController {
    
    private final FundLockService fundLockService;
    
    /**
     * Lock tiền trong tài khoản
     */
    @PostMapping("/lock")
    public ResponseEntity<LockFundsResponse> lockFunds(@Valid @RequestBody LockFundsRequest request) {
        log.info("[CONTROLLER] POST /api/core-banking/fund-locks/lock - Account: {}, Amount: {}", 
                 request.getAccountNumber(), request.getAmount());
        
        LockFundsResponse response = fundLockService.lockFunds(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Unlock tiền đã lock
     */
    @PostMapping("/unlock")
    public ResponseEntity<LockFundsResponse> unlockFunds(@Valid @RequestBody UnlockFundsRequest request) {
        log.info("[CONTROLLER] POST /api/core-banking/fund-locks/unlock - Lock ID: {}", 
                 request.getLockId());
        
        LockFundsResponse response = fundLockService.unlockFunds(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unlock theo reference ID
     */
    @PostMapping("/unlock-by-reference/{referenceId}")
    public ResponseEntity<LockFundsResponse> unlockByReference(
            @PathVariable String referenceId,
            @RequestParam(required = false, defaultValue = "Manual unlock") String reason) {
        
        log.info("[CONTROLLER] POST /api/core-banking/fund-locks/unlock-by-reference/{} - Reason: {}", 
                 referenceId, reason);
        
        LockFundsResponse response = fundLockService.unlockFundsByReference(referenceId, reason);
        return ResponseEntity.ok(response);
    }
}
