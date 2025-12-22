package com.example.loanservice.service;

import com.example.loanservice.dto.response.LoanAccountResponse;

import java.util.List;

/**
 * Loan Account Service - Manage loan contracts
 */
public interface LoanAccountService {
    
    /**
     * Get loan account details
     */
    LoanAccountResponse getLoanAccount(String loanAccountId);
    
    /**
     * Get loan accounts by customer
     */
    List<LoanAccountResponse> getLoanAccountsByCustomer(String cifId);
    
    /**
     * Get active loan accounts by customer
     */
    List<LoanAccountResponse> getActiveLoansByCustomer(String cifId);
    
    /**
     * Update loan status (internal use)
     */
    void updateLoanStatus(String loanAccountId, String status);
}
