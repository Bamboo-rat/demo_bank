package com.example.customerservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.dto.request.CustomerUpdateRequest;
import com.example.customerservice.dto.response.CustomerValidationResponse;
import com.example.customerservice.dto.response.EkycResponse;
import com.example.customerservice.service.CustomerService;
import com.example.customerservice.service.EkycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final EkycService ekycService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyInfo() {
        CustomerResponse customerResponse = customerService.getMyInfo();
        ApiResponse<CustomerResponse> response = ApiResponse.success("Lấy thông tin cá nhân thành công.", customerResponse);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyInfo(@RequestBody @Valid CustomerUpdateRequest updateRequest) {
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authProviderId = principal.getSubject();
        CustomerResponse updated = customerService.updateCustomer(authProviderId, updateRequest);
        ApiResponse<CustomerResponse> response = ApiResponse.success("Cập nhật thông tin thành công.", updated);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kyc/verify")
    public ResponseEntity<ApiResponse<EkycResponse>> verifyKyc(@RequestBody @Valid CustomerRegisterRequest customerData) {
        EkycResponse ekycResponse = ekycService.verifyUser(customerData);
        ApiResponse<EkycResponse> response = ApiResponse.success("eKYC verification completed.", ekycResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate/{customerId}")
    public ResponseEntity<ApiResponse<CustomerValidationResponse>> validateCustomer(
            @PathVariable String customerId,
            @RequestParam(required = false, defaultValue = "false") boolean checkActiveStatus,
            @RequestParam(required = false, defaultValue = "false") boolean checkKycStatus) {
        CustomerResponse customer = customerService.getCustomerById(customerId);
        
        boolean isValid = true;
        String message = "Customer is valid";
        
        if (checkActiveStatus && !"ACTIVE".equals(customer.getStatus())) {
            isValid = false;
            message = "Customer is not active";
        }
        
        if (checkKycStatus && !"VERIFIED".equals(customer.getKycStatus())) {
            isValid = false;
            message = message.equals("Customer is valid") ? "Customer KYC not verified" : message + " and KYC not verified";
        }
        
        CustomerValidationResponse validationResponse = CustomerValidationResponse.builder()
                .customerId(customer.getCustomerId())
                .valid(isValid)
                .message(message)
                .customerName(customer.getFullName())
                .cifNumber(customer.getCoreBankingId())
                .status(customer.getStatus().toString())
                .build();
        
        ApiResponse<CustomerValidationResponse> response = ApiResponse.success("Customer validation completed", validationResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-auth-provider/{authProviderId}")
    public ResponseEntity<ApiResponse<CustomerValidationResponse>> getCustomerByAuthProviderId(
            @PathVariable String authProviderId) {
        CustomerResponse customer = customerService.getCustomerByAuthProviderId(authProviderId);
        
        CustomerValidationResponse validationResponse = CustomerValidationResponse.builder()
                .customerId(customer.getCustomerId())
                .valid(true)
                .message("Customer found")
                .customerName(customer.getFullName())
                .cifNumber(customer.getCoreBankingId())
                .status(customer.getStatus().toString())
                .build();
        
        ApiResponse<CustomerValidationResponse> response = ApiResponse.success("Customer retrieved", validationResponse);
        return ResponseEntity.ok(response);
    }
}