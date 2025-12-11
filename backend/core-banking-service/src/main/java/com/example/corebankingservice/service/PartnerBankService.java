package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.response.PartnerBankAccountResponse;

/**
 * Service for interacting with partner banks
 */
public interface PartnerBankService {

    /**
     * Verify account at partner bank
     * @param bankCode the partner bank code
     * @param accountNumber the account number to verify
     * @return account verification response
     */
    PartnerBankAccountResponse verifyAccount(String bankCode, String accountNumber);

    /**
     * Get account name from partner bank
     * @param bankCode the partner bank code
     * @param accountNumber the account number
     * @return account name or null if not found
     */
    String getAccountName(String bankCode, String accountNumber);
}
