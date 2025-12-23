package com.example.loanservice.service;

import com.example.loanservice.dto.request.LoanApplicationRequest;
import com.example.loanservice.dto.request.LoanApprovalRequest;
import com.example.loanservice.dto.response.LoanApplicationResponse;

import java.util.List;

/**
 * Loan Application Service - Business logic for loan registration and approval
 */
public interface LoanApplicationService {
    
    /**
     * Register new loan application
     * - Verify customer (KYC via Dubbo)
     * - Validate loan amount and term
     * - Create LoanApplication entity
     * - Status: PENDING_APPROVAL
     */
    LoanApplicationResponse registerApplication(LoanApplicationRequest request, String customerId);
    
    /**
     * Approve loan application
     * - Snapshot interest rate
     * - Create LoanAccount
     * - Generate RepaymentSchedule
     * - Publish LOAN_APPROVED event
     */
    LoanApplicationResponse approveLoan(String applicationId, LoanApprovalRequest request);
    
    /**
     * Reject loan application
     */
    LoanApplicationResponse rejectLoan(String applicationId, String rejectionReason);
    
    /**
     * Get loan application by ID
     */
    LoanApplicationResponse getApplication(String applicationId);
    
    /**
     * Get loan applications by customer
     */
    List<LoanApplicationResponse> getApplicationsByCustomer(String cifId);
    
    /**
     * Cancel loan application (only if PENDING)
     */
    LoanApplicationResponse cancelApplication(String applicationId, String reason);
}
