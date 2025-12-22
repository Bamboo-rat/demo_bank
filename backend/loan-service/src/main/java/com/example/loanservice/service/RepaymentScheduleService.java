package com.example.loanservice.service;

import com.example.loanservice.dto.response.RepaymentScheduleResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repayment Schedule Service - Calculate and manage loan repayment schedules
 */
public interface RepaymentScheduleService {
    
    /**
     * Generate repayment schedule after loan approval
     * - Calculate installments based on repayment method
     * - Support: Equal Principal, Annuity (Equal Installment)
     */
    void generateSchedule(String loanAccountId);
    
    /**
     * Get repayment schedule for a loan
     */
    List<RepaymentScheduleResponse> getSchedule(String loanAccountId);
    
    /**
     * Get current pending installment
     */
    RepaymentScheduleResponse getCurrentInstallment(String loanAccountId);
    
    /**
     * Get overdue installments
     */
    List<RepaymentScheduleResponse> getOverdueInstallments(String loanAccountId);
    
    /**
     * Calculate total amount due for early settlement
     */
    BigDecimal calculateEarlySettlementAmount(String loanAccountId);
}
