package com.example.transactionservice.dubbo.consumer;

import com.example.commonapi.dto.account.AccountInfoDTO;
import com.example.commonapi.dubbo.AccountQueryDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * Dubbo consumer for Account Service
 * Used to get account and user information via RPC
 */
@Component
public class AccountServiceClient {

    @DubboReference(
        version = "1.0.0",
        group = "banking-services",
        timeout = 5000,
        retries = 2,
        check = false
    )
    private AccountQueryDubboService accountQueryDubboService;

    /**
     * Get account information by account number
     * @param accountNumber account number
     * @return account info
     */
    public AccountInfoDTO getAccountInfo(String accountNumber) {
        return accountQueryDubboService.getAccountInfoByNumber(accountNumber);
    }

    /**
     * Check if account is active
     * @param accountNumber account number
     * @return true if active
     */
    public boolean isAccountActive(String accountNumber) {
        return accountQueryDubboService.isAccountActive(accountNumber);
    }
}
