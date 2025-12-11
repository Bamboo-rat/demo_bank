package com.example.transactionservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.transactionservice.dto.response.BankResponse;
import com.example.transactionservice.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for bank information
 */
@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    /**
     * Get all supported banks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BankResponse>>> getAllBanks() {
        List<BankResponse> banks = bankService.getAllBanks();
        return ResponseEntity.ok(
                ApiResponse.success("Banks fetched successfully", banks)
        );
    }

    /**
     * Search banks by name, short name, or code
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BankResponse>>> searchBanks(
            @RequestParam String q) {
        List<BankResponse> banks = bankService.searchBanks(q);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", banks)
        );
    }

    /**
     * Get bank by code
     */
    @GetMapping("/code/{bankCode}")
    public ResponseEntity<ApiResponse<BankResponse>> getBankByCode(
            @PathVariable String bankCode) {
        BankResponse bank = bankService.getBankByCode(bankCode);
        if (bank == null) {
            return ResponseEntity.ok(
                    ApiResponse.error("Bank not found with code: " + bankCode, "BANK_NOT_FOUND")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success("Bank fetched successfully", bank)
        );
    }

    /**
     * Get bank by BIN
     */
    @GetMapping("/bin/{bin}")
    public ResponseEntity<ApiResponse<BankResponse>> getBankByBin(
            @PathVariable String bin) {
        BankResponse bank = bankService.getBankByBin(bin);
        if (bank == null) {
            return ResponseEntity.ok(
                    ApiResponse.error("Bank not found with BIN: " + bin, "BANK_NOT_FOUND")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success("Bank fetched successfully", bank)
        );
    }
}
