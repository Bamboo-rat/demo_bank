package com.example.loanservice.controller;

import com.example.loanservice.dto.response.RepaymentScheduleResponse;
import com.example.loanservice.service.RepaymentScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loan/schedule")
@RequiredArgsConstructor
@Slf4j
public class RepaymentScheduleController {
    
    private final RepaymentScheduleService scheduleService;
    
    @GetMapping("/{loanAccountId}")
    public ResponseEntity<List<RepaymentScheduleResponse>> getSchedule(
            @PathVariable String loanAccountId) {
        log.info("[API-SCHEDULE-GET] Getting repayment schedule for loan: {}", loanAccountId);
        List<RepaymentScheduleResponse> response = scheduleService.getSchedule(loanAccountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{loanAccountId}/current")
    public ResponseEntity<RepaymentScheduleResponse> getCurrentInstallment(
            @PathVariable String loanAccountId) {
        log.info("[API-SCHEDULE-CURRENT] Getting current installment for loan: {}", loanAccountId);
        RepaymentScheduleResponse response = scheduleService.getCurrentInstallment(loanAccountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{loanAccountId}/overdue")
    public ResponseEntity<List<RepaymentScheduleResponse>> getOverdueInstallments(
            @PathVariable String loanAccountId) {
        log.info("[API-SCHEDULE-OVERDUE] Getting overdue installments for loan: {}", loanAccountId);
        List<RepaymentScheduleResponse> response = scheduleService.getOverdueInstallments(loanAccountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{loanAccountId}/settlement-amount")
    public ResponseEntity<BigDecimal> calculateEarlySettlementAmount(
            @PathVariable String loanAccountId) {
        log.info("[API-SCHEDULE-SETTLE] Calculating early settlement amount for loan: {}", loanAccountId);
        BigDecimal amount = scheduleService.calculateEarlySettlementAmount(loanAccountId);
        return ResponseEntity.ok(amount);
    }
}
