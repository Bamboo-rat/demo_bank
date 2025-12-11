package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.dto.request.AccountLifecycleActionRequest;
import com.example.corebankingservice.dto.request.AccountStatusUpdateRequest;
import com.example.corebankingservice.dto.request.OpenAccountCoreRequest;
import com.example.corebankingservice.dto.response.AccountDetailResponse;
import com.example.corebankingservice.dto.response.AccountStatusHistoryResponse;
import com.example.corebankingservice.dto.response.AccountStatusResponse;
import com.example.corebankingservice.dto.response.BalanceResponse;
import com.example.corebankingservice.service.AccountLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountLifecycleService accountLifecycleService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDetailResponse>> openAccount(@RequestBody @Valid OpenAccountCoreRequest request) {
        AccountDetailResponse response = accountLifecycleService.openAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account opened successfully", response));
    }

    @PostMapping("/{accountNumber}/close")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> closeAccount(@PathVariable String accountNumber,
                                                                           @RequestBody @Valid AccountLifecycleActionRequest request) {
        AccountDetailResponse response = accountLifecycleService.closeAccount(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Account closed", response));
    }

    @PostMapping("/{accountNumber}/freeze")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> freezeAccount(@PathVariable String accountNumber,
                                                                            @RequestBody @Valid AccountLifecycleActionRequest request) {
        AccountDetailResponse response = accountLifecycleService.freezeAccount(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Account frozen", response));
    }

    @PostMapping("/{accountNumber}/unfreeze")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> unfreezeAccount(@PathVariable String accountNumber,
                                                                              @RequestBody @Valid AccountLifecycleActionRequest request) {
        AccountDetailResponse response = accountLifecycleService.unfreezeAccount(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Account unfrozen", response));
    }

    @PostMapping("/{accountNumber}/block")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> blockAccount(@PathVariable String accountNumber,
                                                                           @RequestBody @Valid AccountLifecycleActionRequest request) {
        AccountDetailResponse response = accountLifecycleService.blockAccount(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Account blocked", response));
    }

    @PostMapping("/{accountNumber}/unblock")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> unblockAccount(@PathVariable String accountNumber,
                                                                             @RequestBody @Valid AccountLifecycleActionRequest request) {
        AccountDetailResponse response = accountLifecycleService.unblockAccount(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Account unblocked", response));
    }

    @GetMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<AccountStatusResponse>> getStatus(@PathVariable String accountNumber) {
        AccountStatusResponse response = accountLifecycleService.getAccountStatus(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account status fetched", response));
    }

    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> updateStatus(@PathVariable String accountNumber,
                                                                           @RequestBody @Valid AccountStatusUpdateRequest request) {
        AccountDetailResponse response = accountLifecycleService.updateAccountStatus(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Account status updated", response));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> getDetail(@PathVariable String accountNumber) {
        AccountDetailResponse response = accountLifecycleService.getAccountDetail(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account detail fetched", response));
    }

    @GetMapping("/{accountNumber}/status-history")
    public ResponseEntity<ApiResponse<List<AccountStatusHistoryResponse>>> getStatusHistory(@PathVariable String accountNumber) {
        List<AccountStatusHistoryResponse> history = accountLifecycleService.getStatusHistory(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account status history fetched", history));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(@PathVariable String accountNumber) {
        BalanceResponse response = accountLifecycleService.getAvailableBalance(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Balance fetched successfully", response));
    }
}
