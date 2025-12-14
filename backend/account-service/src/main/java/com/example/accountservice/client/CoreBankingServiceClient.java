package com.example.accountservice.client;

import com.example.accountservice.dto.response.BalanceResponse;
import com.example.commonapi.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "core-banking-service", url = "${spring.cloud.openfeign.client.config.core-banking-service.url:http://localhost:8088}", path = "/api/account")
public interface CoreBankingServiceClient {

    /**
     * Get account balance from core banking
     * @param accountNumber account number
     * @return balance response
     */
    @GetMapping("/{accountNumber}/balance")
    ApiResponse<BalanceResponse> getBalance(@PathVariable("accountNumber") String accountNumber);
}
