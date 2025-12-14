package com.example.customerservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.request.CustomerUpdateRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.dto.response.CustomerValidationResponse;
import com.example.customerservice.dto.response.EkycResponse;
import com.example.customerservice.service.CustomerService;
import com.example.customerservice.service.EkycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs quản lý thông tin khách hàng")
public class CustomerController {

    private final CustomerService customerService;
    private final EkycService ekycService;

    @Operation(
            summary = "Lấy thông tin khách hàng hiện tại",
            description = "API lấy thông tin chi tiết của khách hàng đang đăng nhập (dựa trên JWT token)",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc token không hợp lệ"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyInfo() {
        CustomerResponse customerResponse = customerService.getMyInfo();
        ApiResponse<CustomerResponse> response = ApiResponse.success("Lấy thông tin cá nhân thành công.", customerResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cập nhật thông tin khách hàng hiện tại",
            description = "API cập nhật thông tin chi tiết của khách hàng đang đăng nhập",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập"
            )
    })
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyInfo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin cập nhật khách hàng",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerUpdateRequest.class))
            )
            @RequestBody @Valid CustomerUpdateRequest updateRequest) {
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authProviderId = principal.getSubject();
        CustomerResponse updated = customerService.updateCustomer(authProviderId, updateRequest);
        ApiResponse<CustomerResponse> response = ApiResponse.success("Cập nhật thông tin thành công.", updated);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xác thực eKYC",
            description = "API xác thực danh tính khách hàng qua eKYC (Electronic Know Your Customer)",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xác thực eKYC thành công",
                    content = @Content(schema = @Schema(implementation = EkycResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Thông tin không hợp lệ hoặc xác thực thất bại"
            )
    })
    @PostMapping("/kyc/verify")
    public ResponseEntity<ApiResponse<EkycResponse>> verifyKyc(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin khách hàng cần xác thực eKYC",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerRegisterRequest.class))
            )
            @RequestBody @Valid CustomerRegisterRequest customerData) {
        EkycResponse ekycResponse = ekycService.verifyUser(customerData);
        ApiResponse<EkycResponse> response = ApiResponse.success("eKYC verification completed.", ekycResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xác thực thông tin khách hàng",
            description = "API kiểm tra tính hợp lệ của khách hàng. **Lưu ý**: Trạng thái và KYC được quản lý bởi Core Banking Service"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xác thực hoàn tất",
                    content = @Content(schema = @Schema(implementation = CustomerValidationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy khách hàng"
            )
    })
    @GetMapping("/validate/{customerId}")
    public ResponseEntity<ApiResponse<CustomerValidationResponse>> validateCustomer(
            @Parameter(description = "ID khách hàng cần xác thực", required = true, example = "CUST-001")
            @PathVariable String customerId,
            @Parameter(description = "Kiểm tra trạng thái hoạt động (được quản lý bởi Core Banking)", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean checkActiveStatus,
            @Parameter(description = "Kiểm tra trạng thái KYC (được quản lý bởi Core Banking)", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean checkKycStatus) {
        CustomerResponse customer = customerService.getCustomerById(customerId);
        
        boolean isValid = true;
        String message = "Customer is valid";
        
        if (checkActiveStatus) {
            log.warn("checkActiveStatus requested but customer status is managed by core banking; skipping local validation for customer {}", customer.getCustomerId());
            isValid = false;
            message = "Customer active status managed in core banking";
        }
        
        if (checkKycStatus) {
            log.warn("checkKycStatus requested but customer KYC is managed by core banking; skipping local validation for customer {}", customer.getCustomerId());
            isValid = false;
            message = message.equals("Customer is valid") ? "Customer KYC verification managed in core banking" : message + "; KYC verification managed in core banking";
        }
        
        CustomerValidationResponse validationResponse = CustomerValidationResponse.builder()
                .customerId(customer.getCustomerId())
                .valid(isValid)
                .message(message)
                .customerName(customer.getFullName())
                .cifNumber(customer.getCifNumber())
                .status("MANAGED_BY_CORE_BANKING")
                .build();
        
        ApiResponse<CustomerValidationResponse> response = ApiResponse.success("Customer validation completed", validationResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Lấy thông tin khách hàng theo Auth Provider ID",
            description = "API lấy thông tin khách hàng dựa trên Auth Provider ID (Keycloak subject)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tìm thấy khách hàng",
                    content = @Content(schema = @Schema(implementation = CustomerValidationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy khách hàng"
            )
    })
    @GetMapping("/by-auth-provider/{authProviderId}")
    public ResponseEntity<ApiResponse<CustomerValidationResponse>> getCustomerByAuthProviderId(
            @Parameter(description = "Auth Provider ID (Keycloak subject)", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable String authProviderId) {
        CustomerResponse customer = customerService.getCustomerByAuthProviderId(authProviderId);
        
        CustomerValidationResponse validationResponse = CustomerValidationResponse.builder()
                .customerId(customer.getCustomerId())
                .valid(true)
                .message("Customer found")
                .customerName(customer.getFullName())
                .cifNumber(customer.getCifNumber())
                .status("MANAGED_BY_CORE_BANKING")
                .build();
        
        ApiResponse<CustomerValidationResponse> response = ApiResponse.success("Customer retrieved", validationResponse);
        return ResponseEntity.ok(response);
    }
}