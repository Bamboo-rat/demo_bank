package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.dto.response.PartnerBankAccountResponse;
import com.example.corebankingservice.service.PartnerBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for partner bank operations
 */
@RestController
@RequestMapping("/api/partner")
@RequiredArgsConstructor
public class PartnerBankController {

    private final PartnerBankService partnerBankService;

    /**
     * Verify account at partner bank (GET endpoint for Feign Client)
     */
    @GetMapping("/{bankCode}/verify/{accountNumber}")
    public ResponseEntity<ApiResponse<PartnerBankAccountResponse>> verifyAccountGet(
            @PathVariable String bankCode,
            @PathVariable String accountNumber) {
        
        PartnerBankAccountResponse response = partnerBankService.verifyAccount(bankCode, accountNumber);
        
        if (response.getExists()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Account verified successfully", response)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.error(response.getMessage())
            );
        }
    }

    /**
     * Verify account at partner bank (POST endpoint)
     */
    @PostMapping("/{bankCode}/verify-account")
    public ResponseEntity<ApiResponse<PartnerBankAccountResponse>> verifyAccount(
            @PathVariable String bankCode,
            @RequestParam String accountNumber) {
        
        PartnerBankAccountResponse response = partnerBankService.verifyAccount(bankCode, accountNumber);
        
        if (response.getExists()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Account verified successfully", response)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.error("Account not found at partner bank", "ACCOUNT_NOT_FOUND")
            );
        }
    }

    /**
     * Get account name from partner bank
     */
    @GetMapping("/{bankCode}/account-name")
    public ResponseEntity<ApiResponse<String>> getAccountName(
            @PathVariable String bankCode,
            @RequestParam String accountNumber) {
        
        String accountName = partnerBankService.getAccountName(bankCode, accountNumber);
        
        if (accountName != null) {
            return ResponseEntity.ok(
                    ApiResponse.success("Account name fetched successfully", accountName)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND")
            );
        }
    }
}
