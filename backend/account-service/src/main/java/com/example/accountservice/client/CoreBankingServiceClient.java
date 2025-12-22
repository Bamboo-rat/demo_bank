package com.example.accountservice.client;

import com.example.accountservice.dto.corebank.CoreBankSavingsCreateRequest;
import com.example.accountservice.dto.corebank.CoreBankSavingsWithdrawRequest;
import com.example.accountservice.dto.corebank.LockFundsRequest;
import com.example.accountservice.dto.corebank.LockFundsResponse;
import com.example.accountservice.dto.response.BalanceResponse;
import com.example.commonapi.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "core-banking-service", url = "${spring.cloud.openfeign.client.config.core-banking-service.url:http://localhost:8088}")
public interface CoreBankingServiceClient {

    /**
     * Get account balance from core banking
     * @param accountNumber account number
     * @return balance response
     */
    @GetMapping("/api/accounts/{accountNumber}/balance")
    ApiResponse<BalanceResponse> getBalance(@PathVariable("accountNumber") String accountNumber);

    /**
     * Create savings account in core banking (lưu thông tin tiền)
     * @param request savings account creation request
     * @return created savings account info
     */
    @PostMapping("/api/accounts/savings/create")
    ApiResponse<String> createSavingsAccount(@RequestBody CoreBankSavingsCreateRequest request);

    /**
     * Process premature withdrawal in core banking
     * @param request withdraw request
     * @return transaction ID
     */
    @PostMapping("/api/accounts/savings/withdraw")
    ApiResponse<String> withdrawSavings(@RequestBody CoreBankSavingsWithdrawRequest request);
    
    /**
     * Lock funds trong tài khoản (cho tiết kiệm, thế chấp, etc.)
     * @param request lock request
     * @return lock response với lock ID
     */
    @PostMapping("/api/core-banking/fund-locks/lock")
    LockFundsResponse lockFunds(@RequestBody LockFundsRequest request);
    
    /**
     * Unlock funds theo reference ID (savings account ID)
     * @param referenceId savings account ID
     * @param reason lý do unlock
     * @return unlock response
     */
    @PostMapping("/api/core-banking/fund-locks/unlock-by-reference/{referenceId}")
    LockFundsResponse unlockFundsByReference(@PathVariable("referenceId") String referenceId,
                                             @RequestParam(name = "reason", required = false) String reason);
}
