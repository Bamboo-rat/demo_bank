package com.example.transactionservice.client;

import com.example.commonapi.dto.ApiResponse;
import com.example.transactionservice.dto.request.BalanceOperationRequest;
import com.example.transactionservice.dto.response.BalanceOperationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Core Banking Service
 * Used for balance operations (debit/credit/hold/release)
 */
@FeignClient(
    name = "core-banking-service",
    url = "${core-banking.service.url:http://localhost:8088}",
    path = "/api/balance"
)
public interface CoreBankingClient {

    /**
     * Debit amount from account
     * @param request balance operation request
     * @return operation response
     */
    @PostMapping("/debit")
    ApiResponse<BalanceOperationResponse> debitBalance(@RequestBody BalanceOperationRequest request);

    /**
     * Credit amount to account
     * @param request balance operation request
     * @return operation response
     */
    @PostMapping("/credit")
    ApiResponse<BalanceOperationResponse> creditBalance(@RequestBody BalanceOperationRequest request);

    /**
     * Hold amount in account
     * @param request balance operation request
     * @return operation response
     */
    @PostMapping("/hold")
    ApiResponse<BalanceOperationResponse> holdAmount(@RequestBody BalanceOperationRequest request);

    /**
     * Release held amount
     * @param request balance operation request
     * @return operation response
     */
    @PostMapping("/release-hold")
    ApiResponse<BalanceOperationResponse> releaseHoldAmount(@RequestBody BalanceOperationRequest request);
}
