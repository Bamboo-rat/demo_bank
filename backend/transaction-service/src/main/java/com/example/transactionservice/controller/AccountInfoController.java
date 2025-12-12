package com.example.transactionservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.account.AccountInfoDTO;
import com.example.transactionservice.dubbo.consumer.AccountServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account Info Controller
 * Provides account information lookup for transfer operations
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountInfoController {

    private final AccountServiceClient accountServiceClient;
    
    @GetMapping("/info/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> getAccountInfo(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String bankCode) {
        
        log.info("Getting account info for: {} at bank: {}", accountNumber, bankCode);

        // For now, only support internal accounts (KienlongBank)
        if (bankCode != null && !bankCode.equals("KIENLONG")) {
            return ResponseEntity.ok(
                ApiResponse.error("External bank account lookup not implemented yet")
            );
        }

        try {
            AccountInfoDTO accountInfo = accountServiceClient.getAccountInfo(accountNumber);
            
            if (accountInfo == null) {
                return ResponseEntity.ok(
                    ApiResponse.error("Account not found")
                );
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Account information retrieved", accountInfo)
            );
        } catch (Exception e) {
            log.error("Error getting account info for {}: {}", accountNumber, e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.error("Unable to retrieve account information: " + e.getMessage())
            );
        }
    }
}
