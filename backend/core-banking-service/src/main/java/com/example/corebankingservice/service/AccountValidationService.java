package com.example.corebankingservice.service;

import com.example.corebankingservice.entity.Account;

/**
 * Service for validating account operations
 */
public interface AccountValidationService {

    /**
     * Validate if account can send money (debit operation)
     * @param accountNumber the account number
     * @throws com.example.corebankingservice.exception.BusinessException if account cannot send money
     */
    void validateCanSendMoney(String accountNumber);

    /**
     * Validate if account can receive money (credit operation)
     * @param accountNumber the account number
     * @throws com.example.corebankingservice.exception.BusinessException if account cannot receive money
     */
    void validateCanReceiveMoney(String accountNumber);

    /**
     * Validate both sender and receiver accounts for transfer
     * @param senderAccountNumber the sender account number
     * @param receiverAccountNumber the receiver account number
     * @throws com.example.corebankingservice.exception.BusinessException if either account cannot participate in transfer
     */
    void validateTransfer(String senderAccountNumber, String receiverAccountNumber);

    /**
     * Check if account can perform debit operations
     * @param account the account entity
     * @return true if account can send money
     */
    boolean canDebit(Account account);

    /**
     * Check if account can perform credit operations
     * @param account the account entity
     * @return true if account can receive money
     */
    boolean canCredit(Account account);
}
