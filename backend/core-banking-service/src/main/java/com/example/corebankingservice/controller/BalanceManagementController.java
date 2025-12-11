package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.dto.request.BalanceOperationRequest;
import com.example.corebankingservice.dto.response.BalanceOperationResponse;
import com.example.corebankingservice.service.BalanceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for balance management operations
 * Source of Truth for all balance changes
 */
@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceManagementController {

    private final BalanceManagementService balanceManagementService;

    /**
     * Debit (subtract) money from account
     * Used by transaction service for withdrawals and transfers
     */
    @PostMapping("/debit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> debit(
            @RequestBody @Valid BalanceOperationRequest request) {
        BalanceOperationResponse response = balanceManagementService.debit(request);
        return ResponseEntity.ok(ApiResponse.success("Debit operation completed", response));
    }

    /**
     * Credit (add) money to account
     * Used by transaction service for deposits and incoming transfers
     */
    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> credit(
            @RequestBody @Valid BalanceOperationRequest request) {
        BalanceOperationResponse response = balanceManagementService.credit(request);
        return ResponseEntity.ok(ApiResponse.success("Credit operation completed", response));
    }

    /**
     * Hold amount for pending transactions
     */
    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> holdAmount(
            @RequestParam String accountNumber,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam String transactionReference) {
        BalanceOperationResponse response = balanceManagementService.holdAmount(
                accountNumber, amount, transactionReference);
        return ResponseEntity.ok(ApiResponse.success("Amount held successfully", response));
    }

    /**
     * Release held amount
     */
    @PostMapping("/release-hold")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> releaseHoldAmount(
            @RequestParam String accountNumber,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam String transactionReference) {
        BalanceOperationResponse response = balanceManagementService.releaseHoldAmount(
                accountNumber, amount, transactionReference);
        return ResponseEntity.ok(ApiResponse.success("Hold amount released successfully", response));
    }
}
