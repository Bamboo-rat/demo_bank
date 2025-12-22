package com.example.corebankingservice.controller;

import com.example.corebankingservice.dto.loan.LoanDisbursementRequest;
import com.example.corebankingservice.dto.loan.LoanDisbursementResponse;
import com.example.corebankingservice.dto.loan.LoanInfoResponse;
import com.example.corebankingservice.dto.loan.LoanRepaymentRequest;
import com.example.corebankingservice.dto.loan.LoanRepaymentResponse;
import com.example.corebankingservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Core Banking Loan Controller
 * Exposes REST endpoints for Loan Service Feign client
 */
@RestController
@RequestMapping("/api/core/loan")
@RequiredArgsConstructor
@Slf4j
public class LoanController {
    
    private final LoanService loanService;
    
    @GetMapping("/generate-number")
    public ResponseEntity<String> generateLoanNumber() {
        log.info("[LOAN-CTRL-001] Generating loan number");
        String loanNumber = loanService.generateLoanNumber();
        return ResponseEntity.ok(loanNumber);
    }
    
    @PostMapping("/create")
    public ResponseEntity<LoanDisbursementResponse> createLoanAccount(
            @RequestBody LoanDisbursementRequest request) {
        log.info("[LOAN-CTRL-002] Creating loan account for loanServiceRef: {}", 
                request.getLoanServiceRef());
        
        LoanDisbursementResponse response = loanService.createLoanAccount(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/disburse/{loanServiceRef}")
    public ResponseEntity<LoanDisbursementResponse> disburseLoan(
            @PathVariable String loanServiceRef) {
        log.info("[LOAN-CTRL-003] Disbursing loan: {}", loanServiceRef);
        
        LoanDisbursementResponse response = loanService.disburseLoan(loanServiceRef);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/repay")
    public ResponseEntity<LoanRepaymentResponse> repayLoan(
            @RequestBody LoanRepaymentRequest request) {
        log.info("[LOAN-CTRL-004] Processing loan repayment for: {}", 
                request.getLoanServiceRef());
        
        LoanRepaymentResponse response = loanService.repayLoan(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/interest/{loanServiceRef}")
    public ResponseEntity<BigDecimal> calculateAccruedInterest(
            @PathVariable String loanServiceRef,
            @RequestParam(required = false) String asOfDate) {
        log.info("[LOAN-CTRL-005] Calculating accrued interest for: {}", loanServiceRef);
        
        LocalDate date = asOfDate != null ? LocalDate.parse(asOfDate) : null;
        BigDecimal interest = loanService.calculateAccruedInterest(loanServiceRef, date);
        return ResponseEntity.ok(interest);
    }
    
    @GetMapping("/{loanServiceRef}")
    public ResponseEntity<LoanInfoResponse> getLoanInfo(
            @PathVariable String loanServiceRef) {
        log.info("[LOAN-CTRL-006] Getting loan info: {}", loanServiceRef);
        
        LoanInfoResponse response = loanService.getLoanInfo(loanServiceRef);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/close/{loanServiceRef}")
    public ResponseEntity<LoanRepaymentResponse> closeLoan(
            @PathVariable String loanServiceRef,
            @RequestParam String accountId) {
        log.info("[LOAN-CTRL-007] Closing loan: {}", loanServiceRef);
        
        LoanRepaymentResponse response = loanService.closeLoan(loanServiceRef, accountId);
        return ResponseEntity.ok(response);
    }
}
