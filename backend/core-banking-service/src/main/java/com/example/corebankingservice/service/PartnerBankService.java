package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.response.PartnerBankAccountResponse;

import java.math.BigDecimal;

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

    /**
     * Send money to partner bank account
     * @param bankCode the partner bank code
     * @param accountNumber the destination account number
     * @param amount the amount to transfer
     * @param transactionReference the transaction reference
     * @param description the transfer description
     * @return transfer response from partner bank
     */
    PartnerBankAccountResponse sendToPartnerBank(String bankCode, String accountNumber, 
                                                 BigDecimal amount, String transactionReference, 
                                                 String description);
}
