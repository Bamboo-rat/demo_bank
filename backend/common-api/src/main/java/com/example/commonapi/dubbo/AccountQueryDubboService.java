package com.example.commonapi.dubbo;

import com.example.commonapi.dto.account.AccountInfoDTO;

/**
 * Dubbo Service Interface for Account Query Operations
 * This interface should be implemented by account-service
 * and referenced by other services (like transaction-service)
 */
public interface AccountQueryDubboService {

    /**
     * Get account information by account number
     * @param accountNumber account number
     * @return account info
     */
    AccountInfoDTO getAccountInfoByNumber(String accountNumber);

    /**
     * Check if account is active
     * @param accountNumber account number
     * @return true if active, false otherwise
     */
    boolean isAccountActive(String accountNumber);
}
