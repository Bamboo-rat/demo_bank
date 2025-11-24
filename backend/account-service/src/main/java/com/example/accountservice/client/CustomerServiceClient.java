package com.example.accountservice.client;

import com.example.accountservice.dto.response.CustomerValidationResponse;
import com.example.commonapi.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "customer-service", path = "/api/customers")
public interface CustomerServiceClient {

    @GetMapping("/validate/{customerId}")
    ApiResponse<CustomerValidationResponse> validateCustomer(
            @PathVariable("customerId") String customerId,
            @RequestParam(name = "checkActiveStatus", required = false, defaultValue = "false") boolean checkActiveStatus,
            @RequestParam(name = "checkKycStatus", required = false, defaultValue = "false") boolean checkKycStatus
    );
}
