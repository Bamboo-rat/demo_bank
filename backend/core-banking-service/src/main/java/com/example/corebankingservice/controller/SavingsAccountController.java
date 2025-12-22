package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.dto.request.SavingsAccountCreationRequest;
import com.example.corebankingservice.dto.request.SavingsWithdrawalRequest;
import com.example.corebankingservice.service.SavingsAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints cho nghiệp vụ tiết kiệm tại Core Banking.
 * Nhận request từ account-service qua Feign client.
 */
@RestController
@RequestMapping("/api/accounts/savings")
@RequiredArgsConstructor
@Slf4j
public class SavingsAccountController {

    private final SavingsAccountService savingsAccountService;

    /**
     * Endpoint tạo savings account trong Core Banking
     * Được gọi từ account-service sau khi đã lock funds
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createSavings(@Valid @RequestBody SavingsAccountCreationRequest request) {
        log.info("[API] POST /api/accounts/savings/create - SavingsID: {}, Amount: {}", 
                request.getSavingsAccountId(), request.getPrincipalAmount());
        
        String savingsAccountId = savingsAccountService.createSavingsAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Savings account recorded in Core Banking", savingsAccountId));
    }

    /**
     * Endpoint xử lý rút trước hạn savings trong Core Banking
     * Được gọi từ account-service để ghi nhận transaction và cộng tiền
     */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdrawSavings(@Valid @RequestBody SavingsWithdrawalRequest request) {
        log.info("[API] POST /api/accounts/savings/withdraw - SavingsID: {}, Total: {}", 
                request.getSavingsAccountId(), request.getTotalAmount());
        
        String transactionId = savingsAccountService.withdrawSavings(request);
        return ResponseEntity.ok(ApiResponse.success("Savings withdrawal processed in Core Banking", transactionId));
    }
}
