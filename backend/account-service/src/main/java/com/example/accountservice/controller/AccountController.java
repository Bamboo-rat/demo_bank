package com.example.accountservice.controller;

import com.example.accountservice.client.CustomerServiceClient;
import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountInfoDTO;
import com.example.accountservice.dto.response.AccountListResponse;
import com.example.accountservice.dto.response.AccountResponse;
import com.example.accountservice.dto.response.CustomerValidationResponse;
import com.example.accountservice.service.AccountService;
import com.example.commonapi.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Account Management", description = "APIs quản lý tài khoản ngân hàng (xem thông tin, danh sách, trạng thái, cập nhật metadata)")
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
    @Operation(
        summary = "Lấy thông tin chi tiết tài khoản",
        description = "Xem thông tin chi tiết tài khoản theo số tài khoản. Chỉ trả về tài khoản thuộc về khách hàng đã xác thực."
    )
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Lấy thông tin tài khoản thành công"),
        @SwaggerApiResponse(responseCode = "403", description = "Không có quyền truy cập tài khoản này"),
        @SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản"),
        @SwaggerApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetails(
            @Parameter(description = "Số tài khoản cần xem", example = "ACC-2024-0001")
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
    @Operation(
        summary = "Lấy danh sách tài khoản của khách hàng",
        description = "Lấy tất cả tài khoản thuộc về khách hàng đã đăng nhập."
    )
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Lấy danh sách tài khoản thành công"),
        @SwaggerApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
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
    @Operation(
        summary = "Kiểm tra trạng thái kích hoạt tài khoản",
        description = "Kiểm tra xem tài khoản có đang ở trạng thái ACTIVE hay không."
    )
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Kiểm tra trạng thái thành công"),
        @SwaggerApiResponse(responseCode = "403", description = "Không có quyền truy cập tài khoản này"),
        @SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản"),
        @SwaggerApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<Boolean>> checkAccountStatus(
            @Parameter(description = "Số tài khoản cần kiểm tra", example = "ACC-2024-0001")
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
    @Operation(
        summary = "Cập nhật thông tin tài khoản",
        description = "Cập nhật metadata của tài khoản (credit limit cho tài khoản tín dụng, lãi suất/kỳ hạn cho tài khoản tiết kiệm). Chỉ cập nhật được tài khoản của chính mình."
    )
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Cập nhật tài khoản thành công"),
        @SwaggerApiResponse(responseCode = "403", description = "Không có quyền cập nhật tài khoản này"),
        @SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản"),
        @SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @SwaggerApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PatchMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @Parameter(description = "Số tài khoản cần cập nhật", example = "ACC-2024-0001")
            @PathVariable String accountNumber,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin cần cập nhật",
                required = true
            )
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

    @Operation(
        summary = "Lấy thông tin tài khoản (Internal API)",
        description = "API nội bộ dùng để lấy thông tin tài khoản cho các service khác (không yêu cầu authentication)."
    )
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Lấy thông tin tài khoản thành công"),
        @SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản")
    })
    @GetMapping("/internal/info/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> getAccountInfoByNumber(
            @Parameter(description = "Số tài khoản", example = "ACC-2024-0001")
            @PathVariable String accountNumber) {

        log.info("Internal request to get account info: {}", accountNumber);

        AccountInfoDTO accountInfo = accountService.getAccountInfoByNumber(accountNumber);

        return ResponseEntity.ok(
            ApiResponse.success("Account information retrieved successfully", accountInfo)
        );
    }
}
