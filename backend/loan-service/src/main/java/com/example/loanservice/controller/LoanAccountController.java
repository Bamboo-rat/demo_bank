package com.example.loanservice.controller;

import com.example.loanservice.dto.response.LoanAccountResponse;
import com.example.loanservice.service.LoanAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan/accounts")
@RequiredArgsConstructor
@Slf4j
public class LoanAccountController {
    
    private final LoanAccountService accountService;
    
    @GetMapping("/{loanAccountId}")
    public ResponseEntity<LoanAccountResponse> getLoanAccount(
            @PathVariable String loanAccountId) {
        log.info("[API-ACCOUNT-GET] Getting loan account: {}", loanAccountId);
        LoanAccountResponse response = accountService.getLoanAccount(loanAccountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/{cifId}")
    public ResponseEntity<List<LoanAccountResponse>> getLoanAccountsByCustomer(
            @PathVariable String cifId) {
        log.info("[API-ACCOUNT-LIST] Getting loan accounts for customer: {}", cifId);
        List<LoanAccountResponse> response = accountService.getLoanAccountsByCustomer(cifId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/{cifId}/active")
    public ResponseEntity<List<LoanAccountResponse>> getActiveLoansByCustomer(
            @PathVariable String cifId) {
        log.info("[API-ACCOUNT-ACTIVE] Getting active loans for customer: {}", cifId);
        List<LoanAccountResponse> response = accountService.getActiveLoansByCustomer(cifId);
        return ResponseEntity.ok(response);
    }
}
