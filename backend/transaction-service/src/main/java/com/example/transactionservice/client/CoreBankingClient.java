package com.example.transactionservice.client;

import com.example.commonapi.dto.ApiResponse;
import com.example.transactionservice.dto.request.TransferExecutionRequest;
import com.example.transactionservice.dto.response.BalanceResponse;
import com.example.transactionservice.dto.response.TransferExecutionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Core Banking Service
 * Used for balance queries and transfer execution
 */
@FeignClient(
    name = "core-banking-service",
    url = "${core-banking.service.url:http://localhost:8088}"
)
public interface CoreBankingClient {

    /**
     * Get account balance
     * @param accountNumber account number
     * @return balance response with available balance
     */
    @GetMapping("/api/accounts/{accountNumber}/balance")
    ApiResponse<BalanceResponse> getBalance(@PathVariable("accountNumber") String accountNumber);

    /**
     * Execute complete transfer (debit + credit + record transaction in Core Banking)
     * This is the recommended method for transfers to ensure transaction is properly recorded
     * @param request transfer execution request
     * @return transfer execution response with Core Banking transaction ID
     */
    @PostMapping("/api/balance/execute-transfer")
    ApiResponse<TransferExecutionResponse> executeTransfer(@RequestBody TransferExecutionRequest request);
}