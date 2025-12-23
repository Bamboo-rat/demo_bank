package com.example.loanservice.controller;

import com.example.loanservice.dto.request.LoanApplicationRequest;
import com.example.loanservice.dto.request.LoanApprovalRequest;
import com.example.loanservice.dto.response.LoanApplicationResponse;
import com.example.loanservice.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan/applications")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {
    
    private final LoanApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> registerApplication(
            @Valid @RequestBody LoanApplicationRequest request,
            Authentication authentication) {
        String authProviderId = extractAuthProviderId(authentication);
        log.info("[API-APP-REGISTER] Registering loan application for authProviderId: {}", authProviderId);
        LoanApplicationResponse response = applicationService.registerApplication(request, authProviderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<LoanApplicationResponse> approveLoan(
            @PathVariable String applicationId,
            @Valid @RequestBody LoanApprovalRequest request) {
        log.info("[API-APP-APPROVE] Approving loan application: {}", applicationId);
        LoanApplicationResponse response = applicationService.approveLoan(applicationId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<LoanApplicationResponse> rejectLoan(
            @PathVariable String applicationId,
            @RequestParam String rejectionReason) {
        log.info("[API-APP-REJECT] Rejecting loan application: {}", applicationId);
        LoanApplicationResponse response = applicationService.rejectLoan(applicationId, rejectionReason);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{applicationId}/cancel")
    public ResponseEntity<LoanApplicationResponse> cancelApplication(
            @PathVariable String applicationId,
            @RequestParam String reason) {
        log.info("[API-APP-CANCEL] Cancelling loan application: {}", applicationId);
        LoanApplicationResponse response = applicationService.cancelApplication(applicationId, reason);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationResponse> getApplication(
            @PathVariable String applicationId) {
        log.info("[API-APP-GET] Getting loan application: {}", applicationId);
        LoanApplicationResponse response = applicationService.getApplication(applicationId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/me")
    public ResponseEntity<List<LoanApplicationResponse>> getApplicationsByCustomer(
            Authentication authentication) {
        String authProviderId = extractAuthProviderId(authentication);
        log.info("[API-APP-LIST] Getting loan applications for authProviderId: {}", authProviderId);
        List<LoanApplicationResponse> response = applicationService.getApplicationsByCustomer(authProviderId);
        return ResponseEntity.ok(response);
    }

    private String extractAuthProviderId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        throw new IllegalStateException("Missing authentication principal for loan request");
    }
}
