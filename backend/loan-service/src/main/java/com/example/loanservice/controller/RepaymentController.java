package com.example.loanservice.controller;

import com.example.loanservice.dto.request.DisbursementRequest;
import com.example.loanservice.dto.request.EarlySettlementRequest;
import com.example.loanservice.dto.request.RepaymentRequest;
import com.example.loanservice.dto.response.DisbursementResponse;
import com.example.loanservice.dto.response.LoanPaymentHistoryResponse;
import com.example.loanservice.dto.response.RepaymentResponse;
import com.example.loanservice.service.RepaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan/repayment")
@RequiredArgsConstructor
@Slf4j
public class RepaymentController {
    
    private final RepaymentService repaymentService;
    
    @PostMapping("/disburse")
    public ResponseEntity<DisbursementResponse> disburseLoan(
            @Valid @RequestBody DisbursementRequest request) {
        log.info("[API-DISBURSE] Disbursing loan: {}", request.getLoanAccountId());
        DisbursementResponse response = repaymentService.disburseLoan(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/pay")
    public ResponseEntity<RepaymentResponse> repayInstallment(
            @Valid @RequestBody RepaymentRequest request) {
        log.info("[API-REPAY] Processing repayment for loan: {}", request.getLoanAccountId());
        RepaymentResponse response = repaymentService.repayInstallment(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/settle")
    public ResponseEntity<RepaymentResponse> earlySettlement(
            @Valid @RequestBody EarlySettlementRequest request) {
        log.info("[API-SETTLE] Processing early settlement for loan: {}", request.getLoanAccountId());
        RepaymentResponse response = repaymentService.earlySettlement(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history/{loanAccountId}")
    public ResponseEntity<List<LoanPaymentHistoryResponse>> getPaymentHistory(
            @PathVariable String loanAccountId) {
        log.info("[API-HISTORY] Getting payment history for loan: {}", loanAccountId);
        List<LoanPaymentHistoryResponse> response = repaymentService.getPaymentHistory(loanAccountId);
        return ResponseEntity.ok(response);
    }
}
