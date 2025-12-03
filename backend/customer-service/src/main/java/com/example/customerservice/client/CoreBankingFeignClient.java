package com.example.customerservice.client;

import com.example.commonapi.dto.ApiResponse;
import com.example.customerservice.dto.request.CreateCoreCustomerRequest;
import com.example.customerservice.dto.response.CreateCoreCustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "core-banking-service", url = "${core-banking.base-url}")
public interface CoreBankingFeignClient {

    @PostMapping("/api/cif/create")
    ApiResponse<CreateCoreCustomerResponse> createCif(@RequestBody CreateCoreCustomerRequest request);

    @DeleteMapping("/api/cif/{cifId}")
    ApiResponse<Void> deleteCif(@PathVariable("cifId") String cifId);
}
