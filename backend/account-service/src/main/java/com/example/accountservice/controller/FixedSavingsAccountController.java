package com.example.accountservice.controller;

import com.example.accountservice.exception.InvalidSavingsOperationException;
import com.example.accountservice.exception.SavingsAccountNotFoundException;
import com.example.accountservice.service.FixedSavingsAccountService;
import com.example.accountservice.dto.savings.*;
import com.example.commonapi.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
@Slf4j
public class FixedSavingsAccountController {

    private final FixedSavingsAccountService savingsService;

    /**
     * Xem trước thông tin tiết kiệm (preview lãi suất, tiền lãi dự kiến, ngày đáo hạn)
     */
    @PostMapping("/calculate-preview")
    public ResponseEntity<ApiResponse<SavingsPreviewResponse>> calculatePreview(
            @Valid @RequestBody SavingsPreviewRequest request) {
        
        log.info("[CONTROLLER] POST /api/savings/calculate-preview - Request: {}", request);
        
        try {
            SavingsPreviewResponse response = savingsService.calculatePreview(request);
            return ResponseEntity.ok(ApiResponse.success("Preview calculated successfully", response));
                    
        } catch (InvalidSavingsOperationException e) {
            log.error("[CONTROLLER] Error calculating preview: {} - {}", e.getErrorCode(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
                    
        } catch (Exception e) {
            log.error("[CONTROLLER] Unexpected error calculating preview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SAVINGS_PREVIEW_FAILED", "Failed to calculate preview"));
        }
    }

    /**
     * Mở sổ tiết kiệm kỳ hạn mới
     */
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> openSavingsAccount(
            @Valid @RequestBody OpenSavingsRequest request,
            Authentication authentication) {
        
        log.info("[CONTROLLER] POST /api/savings/open - Request: {}", request);
        
        try {
            String customerId = extractCustomerId(authentication);
            SavingsAccountResponse response = savingsService.openSavingsAccount(request, customerId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Savings account opened successfully", response));
                    
        } catch (InvalidSavingsOperationException e) {
            log.error("[CONTROLLER] Error opening savings account: {} - {}", e.getErrorCode(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
                    
        } catch (Exception e) {
            log.error("[CONTROLLER] Unexpected error opening savings account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SAVINGS_OPEN_FAILED", "Failed to open savings account"));
        }
    }

    /**
     * Lấy thông tin chi tiết sổ tiết kiệm
     */
    @GetMapping("/{savingsAccountId}")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> getSavingsAccount(
            @PathVariable String savingsAccountId,
            Authentication authentication) {
        
        log.info("[CONTROLLER] GET /api/savings/{} - customerId: {}", 
                savingsAccountId, extractCustomerId(authentication));
        
        try {
            String customerId = extractCustomerId(authentication);
            SavingsAccountResponse response = savingsService.getSavingsAccountById(savingsAccountId, customerId);
            
            return ResponseEntity.ok(ApiResponse.success("Savings account fetched successfully", response));
            
        } catch (SavingsAccountNotFoundException e) {
            log.error("[CONTROLLER] Savings account not found: {}", savingsAccountId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
                    
        } catch (Exception e) {
            log.error("[CONTROLLER] Error fetching savings account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SAVINGS_FETCH_FAILED", "Failed to fetch savings account"));
        }
    }

    /**
     * Lấy danh sách tất cả sổ tiết kiệm của khách hàng
     */
    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<List<SavingsAccountResponse>>> getCustomerSavingsAccounts(
            Authentication authentication) {
        
        String customerId = extractCustomerId(authentication);
        log.info("[CONTROLLER] GET /api/savings/customer - customerId: {}", customerId);
        
        try {
            List<SavingsAccountResponse> responses = savingsService.getCustomerSavingsAccounts(customerId);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    String.format("Found %d savings accounts", responses.size()),
                    responses)
            );
            
        } catch (Exception e) {
            log.error("[CONTROLLER] Error fetching customer savings accounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SAVINGS_LIST_FAILED", "Failed to fetch savings accounts"));
        }
    }

    /**
     * Rút tiền trước hạn (mất lãi ưu đãi)
     */
    @PostMapping("/{savingsAccountId}/premature-withdraw")
    public ResponseEntity<ApiResponse<PrematureWithdrawResponse>> prematureWithdraw(
            @PathVariable String savingsAccountId,
            Authentication authentication) {
        
        log.info("[CONTROLLER] POST /api/savings/{}/premature-withdraw - customerId: {}", 
                savingsAccountId, extractCustomerId(authentication));
        
        try {
            String customerId = extractCustomerId(authentication);
            PrematureWithdrawResponse response = savingsService.prematureWithdraw(savingsAccountId, customerId);
            
            return ResponseEntity.ok(ApiResponse.success("Premature withdrawal processed successfully", response));
            
        } catch (SavingsAccountNotFoundException e) {
            log.error("[CONTROLLER] Savings account not found: {}", savingsAccountId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
                    
        } catch (InvalidSavingsOperationException e) {
            log.error("[CONTROLLER] Invalid withdrawal operation: {} - {}", e.getErrorCode(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
                    
        } catch (Exception e) {
            log.error("[CONTROLLER] Error processing premature withdrawal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SAVINGS_WITHDRAW_FAILED", "Failed to process withdrawal"));
        }
    }

    /**
     * Extract customerId từ JWT token
     */
    private String extractCustomerId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject(); // UUID from Keycloak
        }
        throw new IllegalStateException("Unable to extract customer ID from authentication");
    }
}
