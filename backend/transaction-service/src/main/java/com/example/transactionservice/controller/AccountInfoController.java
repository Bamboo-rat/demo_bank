package com.example.transactionservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.account.AccountInfoDTO;
import com.example.transactionservice.dto.response.PartnerBankAccountResponse;
import com.example.transactionservice.dubbo.consumer.AccountServiceClient;
import com.example.transactionservice.service.BankService;
import com.example.transactionservice.client.CoreBankingClient;
import com.example.transactionservice.dto.response.BankResponse;
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
    private final CoreBankingClient coreBankingClient;
    private final BankService bankService;
    
    @GetMapping("/info/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> getAccountInfo(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String bankCode) {
        
        log.info("Getting account info for: {} at bank: {}", accountNumber, bankCode);

        try {
            // Internal transfer (no bankCode or KIENLONG)
            if (bankCode == null || bankCode.equals("KIENLONG")) {
                AccountInfoDTO accountInfo = accountServiceClient.getAccountInfo(accountNumber);
                
                if (accountInfo == null) {
                    return ResponseEntity.ok(
                        ApiResponse.error("Account not found")
                    );
                }
                
                return ResponseEntity.ok(
                    ApiResponse.success("Account information retrieved", accountInfo)
                );
            }
            
            // External bank transfer - verify through Core Banking Service
            log.info("Looking up external account {} at bank {}", accountNumber, bankCode);
            
            ApiResponse<PartnerBankAccountResponse> externalResponse =
                coreBankingClient.verifyExternalAccount(bankCode, accountNumber);
            
            if (!externalResponse.isSuccess() || externalResponse.getData() == null) {
                return ResponseEntity.ok(
                    ApiResponse.error("Unable to verify external account: " + externalResponse.getMessage())
                );
            }
            
            PartnerBankAccountResponse partnerResponse = externalResponse.getData();
            
            if (!partnerResponse.getExists()) {
                return ResponseEntity.ok(
                    ApiResponse.error("Account not found at partner bank")
                );
            }
            
            if (!partnerResponse.getActive()) {
                return ResponseEntity.ok(
                    ApiResponse.error("Account is not active at partner bank")
                );
            }
            
            // Get bank info
            BankResponse bankInfo = bankService.getBankByCode(bankCode);
            
            // Convert to AccountInfoDTO
            AccountInfoDTO accountInfo = AccountInfoDTO.builder()
                .accountNumber(partnerResponse.getAccountNumber())
                .accountHolderName(partnerResponse.getAccountName())
                .bankCode(partnerResponse.getBankCode())
                .bankName(bankInfo != null ? bankInfo.getName() : partnerResponse.getBankName())
                .isActive(partnerResponse.getActive())
                .build();
            
            return ResponseEntity.ok(
                ApiResponse.success("External account information retrieved", accountInfo)
            );
            
        } catch (Exception e) {
            log.error("Error getting account info for {}: {}", accountNumber, e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.error("Unable to retrieve account information: " + e.getMessage())
            );
        }
    }
}
