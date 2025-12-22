package com.example.loanservice.client;

import com.example.loanservice.dto.corebanking.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "core-banking-service", url = "${service.core-banking.url}")
public interface CoreBankingClient {
    
    /**
     * Generate loan account number
     */
    @GetMapping("/api/core/loan/generate-number")
    String generateLoanNumber();
    
    /**
     * Create loan account in Core Banking ledger
     */
    @PostMapping("/api/core/loan/create")
    CoreLoanDisbursementResponse createLoanAccount(@RequestBody CoreLoanDisbursementRequest request);
    
    /**
     * Disburse loan - credit money to customer account
     */
    @PostMapping("/api/core/loan/disburse/{loanServiceRef}")
    CoreLoanDisbursementResponse disburseLoan(@PathVariable String loanServiceRef);
    
    /**
     * Repay loan - debit money from customer account
     */
    @PostMapping("/api/core/loan/repay")
    CoreLoanRepaymentResponse repayLoan(@RequestBody CoreLoanRepaymentRequest request);
    
    /**
     * Calculate accrued interest
     */
    @GetMapping("/api/core/loan/interest/{loanServiceRef}")
    CoreAccruedInterestResponse calculateAccruedInterest(
            @PathVariable String loanServiceRef,
            @RequestParam(required = false) String asOfDate);
    
    /**
     * Get loan information from Core Banking
     */
    @GetMapping("/api/core/loan/{loanServiceRef}")
    CoreLoanInfoResponse getLoanInfo(@PathVariable String loanServiceRef);
    
    /**
     * Close loan with early settlement
     */
    @PostMapping("/api/core/loan/close/{loanServiceRef}")
    CoreLoanRepaymentResponse closeLoan(
            @PathVariable String loanServiceRef,
            @RequestParam String accountId);
}
