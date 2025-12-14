package com.example.transactionservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.transactionservice.dto.request.TransferConfirmDTO;
import com.example.transactionservice.dto.request.TransferRequestDTO;
import com.example.transactionservice.dto.response.TransferResponseDTO;
import com.example.transactionservice.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Transfer Controller
 * Handles money transfer operations with OTP verification
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Step 1: Initiate transfer and generate OTP
     * POST /api/transactions/transfer/request
     */
    @PostMapping("/transfer/request")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> initiateTransfer(
            @Valid @RequestBody TransferRequestDTO request) {
        
        log.info("Initiating transfer from {} to {}", 
            request.getSourceAccountNumber(), 
            request.getDestinationAccountNumber());

        TransferResponseDTO response = transferService.initiateTransfer(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Transfer initiated. OTP sent to your phone.", response));
    }

    /**
     * Step 2: Confirm transfer with OTP
     * POST /api/transactions/transfer/confirm
     */
    @PostMapping("/transfer/confirm")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> confirmTransfer(
            @Valid @RequestBody TransferConfirmDTO confirmDTO) {
        
        log.info("✅ Confirming transfer: {}", confirmDTO.getTransactionId());

        TransferResponseDTO response = transferService.confirmTransfer(confirmDTO);
        
        return ResponseEntity.ok(
            ApiResponse.success("Transfer completed successfully", response)
        );
    }

    /**
     * Get transaction details by ID
     * GET /api/transactions/{transactionId}
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getTransaction(
            @PathVariable String transactionId) {
        
        log.info("🔍 Getting transaction: {}", transactionId);

        TransferResponseDTO response = transferService.getTransactionById(transactionId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Transaction retrieved", response)
        );
    }

    /**
     * Get transaction history for account
     * GET /api/transactions/account/{accountNumber}?page=0&size=10
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("📜 Getting transaction history for account: {}", accountNumber);

        List<TransferResponseDTO> transactions = transferService.getTransactionHistory(
            accountNumber, page, size
        );
        
        return ResponseEntity.ok(
            ApiResponse.success("Transaction history retrieved", transactions)
        );
    }

    /**
     * Cancel pending transaction
     * PUT /api/transactions/{transactionId}/cancel
     */
    @PutMapping("/{transactionId}/cancel")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> cancelTransaction(
            @PathVariable String transactionId) {
        
        log.info("🚫 Cancelling transaction: {}", transactionId);

        TransferResponseDTO response = transferService.cancelTransaction(transactionId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Transaction cancelled", response)
        );
    }

    // TODO: [KAFKA] Add webhook endpoint for external bank callbacks
    // @PostMapping("/webhook/external-bank")
    // public ResponseEntity<ApiResponse<Void>> handleExternalBankCallback(
    //         @RequestBody ExternalBankCallbackDTO callback) {
    //     // Handle async response from external banks
    //     // Update transaction status
    //     // Send notification via Kafka
    //     return ResponseEntity.ok(ApiResponse.success("Callback processed", null));
    // }
}
