package com.example.accountservice.controller;

import com.example.accountservice.client.CustomerServiceClient;
import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountListResponse;
import com.example.accountservice.dto.response.AccountResponse;
import com.example.accountservice.dto.response.CustomerValidationResponse;
import com.example.accountservice.service.AccountService;
import com.example.commonapi.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Account Management Operations (Read-Only)
 *
 * Provides endpoints for:
 * - Viewing account details
 * - Listing customer accounts
 * - Checking account balance
 * - Updating account metadata
 *
 * Note: Account creation/closure is handled by CustomerService orchestrator
 * calling CoreBankingService, then syncing metadata to AccountService via Dubbo.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final CustomerServiceClient customerServiceClient;

    /**
     * Get account details by account number
     *
     * GET /api/accounts/{accountNumber}
     *
     * Security: Requires USER role
     * Only returns account if it belongs to authenticated customer
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetails(
            @PathVariable String accountNumber) {

        log.info("Request to get account details: {}", accountNumber);

        AccountResponse account = accountService.getAccountDetails(accountNumber);

        // Security check: Ensure account belongs to authenticated customer
        String authenticatedCustomerId = getAuthenticatedCustomerId();
        if (!account.getCustomerId().equals(authenticatedCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this account"));
        }

        return ResponseEntity.ok(
            ApiResponse.success("Account details retrieved successfully", account)
        );
    }

    /**
     * Get all accounts for authenticated customer
     *
     * GET /api/accounts/my-accounts
     *
     * Security: Requires USER role
     * Returns only accounts belonging to authenticated customer
     */
    @GetMapping("/my-accounts")
    public ResponseEntity<ApiResponse<AccountListResponse>> getMyAccounts() {

        String customerId = getAuthenticatedCustomerId();
        log.info("Request to get all accounts for customer: {}", customerId);

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);

        AccountListResponse response = AccountListResponse.builder()
                .customerId(customerId)
                .accounts(accounts)
                .totalCount(accounts.size())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(
            ApiResponse.success(
                String.format("Found %d account(s)", accounts.size()),
                response
            )
        );
    }

    /**
     * Check if account is active
     *
     * GET /api/accounts/{accountNumber}/status
     *
     * Security: Requires USER role
     */
    @GetMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<Boolean>> checkAccountStatus(
            @PathVariable String accountNumber) {

        log.info("Request to check status for account: {}", accountNumber);

        // Verify ownership
        AccountResponse account = accountService.getAccountDetails(accountNumber);
        String authenticatedCustomerId = getAuthenticatedCustomerId();

        if (!account.getCustomerId().equals(authenticatedCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this account"));
        }

        boolean isActive = accountService.isAccountActive(accountNumber);

        return ResponseEntity.ok(
            ApiResponse.success(
                isActive ? "Account is active" : "Account is not active",
                isActive
            )
        );
    }

    /**
     * Update account information
     *
     * PATCH /api/accounts/{accountNumber}
     *
     * Security: Requires USER role
     * Can only update own accounts
     *
     * Supports:
     * - Update credit limit (Credit accounts)
     * - Update interest rate (Savings accounts)
     * - Update term months (Savings accounts)
     */
    @PatchMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody UpdateAccountRequest request) {

        String customerId = getAuthenticatedCustomerId();
        log.info("Request to update account: {} for customer: {}", accountNumber, customerId);

        // Verify ownership is done in service layer
        AccountResponse updated = accountService.updateAccount(accountNumber, customerId, request);

        return ResponseEntity.ok(
            ApiResponse.success("Account updated successfully", updated)
        );
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Extract authenticated customer ID from JWT token
     * SECURITY: Uses authProviderId from JWT to lookup actual customerId from customer-service
     */
    private String getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No JWT authentication found");
        }

        // Get authProviderId from JWT (Keycloak user ID)
        String authProviderId = jwt.getSubject();
        if (authProviderId == null || authProviderId.isBlank()) {
            throw new IllegalStateException("authProviderId (sub claim) not found in JWT");
        }

        try {

            ApiResponse<CustomerValidationResponse> response = customerServiceClient.getCustomerByAuthProviderId(authProviderId);
            
            CustomerValidationResponse customerData = response.getData();
            if (customerData == null) {
                throw new IllegalStateException("Customer not found for authProviderId: " + authProviderId);
            }

            return customerData.getCustomerId();
            
        } catch (Exception e) {
            log.error("Failed to resolve customerId for authProviderId: {}", authProviderId, e);
            throw new IllegalStateException("Unable to resolve customerId: " + e.getMessage(), e);
        }
    }
}
