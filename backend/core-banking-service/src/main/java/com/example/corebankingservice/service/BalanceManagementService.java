package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.request.BalanceOperationRequest;
import com.example.corebankingservice.dto.response.BalanceOperationResponse;

/**
 * Service for managing account balance operations
 * This is the Source of Truth for all balance changes
 */
public interface BalanceManagementService {

    /**
     * Debit (subtract) money from account
     * Validates available balance before deducting
     * Creates audit log
     * 
     * @param request the debit operation request
     * @return balance operation response
     * @throws com.example.corebankingservice.exception.BusinessException if insufficient balance
     */
    BalanceOperationResponse debit(BalanceOperationRequest request);

    /**
     * Credit (add) money to account
     * Creates audit log
     * 
     * @param request the credit operation request
     * @return balance operation response
     */
    BalanceOperationResponse credit(BalanceOperationRequest request);

    /**
     * Hold (freeze) amount for pending transactions
     * Moves money from available balance to hold amount
     * 
     * @param accountNumber the account number
     * @param amount the amount to hold
     * @param transactionReference transaction reference
     * @return operation response
     */
    BalanceOperationResponse holdAmount(String accountNumber, java.math.BigDecimal amount, String transactionReference);

    /**
     * Release held amount back to available balance
     * 
     * @param accountNumber the account number
     * @param amount the amount to release
     * @param transactionReference transaction reference
     * @return operation response
     */
    BalanceOperationResponse releaseHoldAmount(String accountNumber, java.math.BigDecimal amount, String transactionReference);
}
