package com.example.loanservice.service;

import com.example.loanservice.dto.request.DisbursementRequest;
import com.example.loanservice.dto.request.RepaymentRequest;
import com.example.loanservice.dto.request.EarlySettlementRequest;
import com.example.loanservice.dto.response.DisbursementResponse;
import com.example.loanservice.dto.response.RepaymentResponse;
import com.example.loanservice.dto.response.LoanPaymentHistoryResponse;

import java.util.List;

/**
 * Repayment Service - Coordinate payment operations with Core Banking
 */
public interface RepaymentService {
    
    /**
     * Disburse loan - coordinate with Core Banking to credit money
     * - Call Core Banking to disburse
     * - Update loan status to ACTIVE
     * - Publish LOAN_DISBURSED event
     */
    DisbursementResponse disburseLoan(DisbursementRequest request);
    
    /**
     * Repay loan installment
     * - Calculate payment obligation
     * - Call Core Banking to debit money
     * - Update installment status
     * - Create payment history
     * - Publish REPAYMENT_SUCCESS event
     */
    RepaymentResponse repayInstallment(RepaymentRequest request);
    
    /**
     * Early settlement - pay off entire loan
     * - Calculate total outstanding
     * - Call Core Banking to close loan
     * - Update all remaining installments to CANCELLED
     * - Publish LOAN_CLOSED event
     */
    RepaymentResponse earlySettlement(EarlySettlementRequest request);
    
    /**
     * Get payment history for a loan
     */
    List<LoanPaymentHistoryResponse> getPaymentHistory(String loanAccountId);
}
