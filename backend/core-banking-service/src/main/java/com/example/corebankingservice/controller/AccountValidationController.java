package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.service.AccountValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/validation")
@RequiredArgsConstructor
public class AccountValidationController {

    private final AccountValidationService accountValidationService;

    /**
     * Validate if account can send money
     */
    @GetMapping("/{accountNumber}/can-send")
    public ResponseEntity<ApiResponse<Boolean>> canSend(@PathVariable String accountNumber) {
        accountValidationService.validateCanSendMoney(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account can send money", true));
    }

    /**
     * Validate if account can receive money
     */
    @GetMapping("/{accountNumber}/can-receive")
    public ResponseEntity<ApiResponse<Boolean>> canReceive(@PathVariable String accountNumber) {
        accountValidationService.validateCanReceiveMoney(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account can receive money", true));
    }

    /**
     * Validate transfer between two accounts
     */
    @GetMapping("/transfer")
    public ResponseEntity<ApiResponse<Boolean>> validateTransfer(
            @RequestParam String senderAccountNumber,
            @RequestParam String receiverAccountNumber) {
        accountValidationService.validateTransfer(senderAccountNumber, receiverAccountNumber);
        return ResponseEntity.ok(ApiResponse.success("Transfer validation passed", true));
    }
}
