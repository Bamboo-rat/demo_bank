package com.example.loanservice.controller;

import com.example.loanservice.dto.response.LoanAccountResponse;
import com.example.loanservice.service.LoanAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
    
    @GetMapping("/customer/me")
    public ResponseEntity<List<LoanAccountResponse>> getLoanAccountsByCustomer(
            Authentication authentication) {
        String authProviderId = extractAuthProviderId(authentication);
        log.info("[API-ACCOUNT-LIST] Getting loan accounts for authProviderId: {}", authProviderId);
        List<LoanAccountResponse> response = accountService.getLoanAccountsByCustomer(authProviderId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/me/active")
    public ResponseEntity<List<LoanAccountResponse>> getActiveLoansByCustomer(
            Authentication authentication) {
        String authProviderId = extractAuthProviderId(authentication);
        log.info("[API-ACCOUNT-ACTIVE] Getting active loans for authProviderId: {}", authProviderId);
        List<LoanAccountResponse> response = accountService.getActiveLoansByCustomer(authProviderId);
        return ResponseEntity.ok(response);
    }

    private String extractAuthProviderId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        throw new IllegalStateException("Missing authentication principal for loan request");
    }
}
