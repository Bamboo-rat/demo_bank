package com.example.loanservice.service.impl;

import com.example.loanservice.client.CustomerServiceClient;
import com.example.loanservice.dto.customer.CustomerInfoResponse;
import com.example.loanservice.dto.response.LoanAccountResponse;
import com.example.loanservice.entity.LoanAccount;
import com.example.loanservice.entity.enums.LoanStatus;
import com.example.loanservice.exception.ErrorCode;
import com.example.loanservice.exception.LoanServiceException;
import com.example.loanservice.mapper.LoanAccountMapper;
import com.example.loanservice.repository.LoanAccountRepository;
import com.example.loanservice.service.LoanAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanAccountServiceImpl implements LoanAccountService {
    
    private final LoanAccountRepository accountRepository;
    private final LoanAccountMapper accountMapper;
    private final CustomerServiceClient customerServiceClient;
    
    @Override
    public LoanAccountResponse getLoanAccount(String loanAccountId) {
        log.info("[LOAN-GET-001] Getting loan account: {}", loanAccountId);
        
        LoanAccount account = accountRepository.findById(loanAccountId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, "Loan ID: " + loanAccountId));
        
        return accountMapper.toResponse(account);
    }
    
    @Override
    public List<LoanAccountResponse> getLoanAccountsByCustomer(String authProviderId) {
        log.info("[LOAN-LIST-001] Getting loan accounts for authProviderId: {}", authProviderId);

        String customerId = resolveCustomerId(authProviderId);
        List<LoanAccount> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }
    
    @Override
    public List<LoanAccountResponse> getActiveLoansByCustomer(String authProviderId) {
        log.info("[LOAN-ACTIVE-001] Getting active loans for authProviderId: {}", authProviderId);

        String customerId = resolveCustomerId(authProviderId);
        List<LoanAccount> accounts = accountRepository.findByCustomerIdAndStatusIn(
                customerId, List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));
        
        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }
    
    @Override
    @Transactional
    public void updateLoanStatus(String loanAccountId, String status) {
        log.info("[LOAN-STATUS-001] Updating loan status: {} to {}", loanAccountId, status);
        
        LoanAccount account = accountRepository.findById(loanAccountId)
                .orElseThrow(() -> new LoanServiceException(ErrorCode.ACC_001, "Loan ID: " + loanAccountId));
        
        account.setStatus(LoanStatus.valueOf(status));
        accountRepository.save(account);
        
        log.info("[LOAN-STATUS-002] Loan status updated successfully");
    }

    private String resolveCustomerId(String authProviderId) {
        CustomerInfoResponse customer = customerServiceClient.getCustomerInfoByAuthProviderId(authProviderId);
        if (customer == null) {
            throw new LoanServiceException(ErrorCode.CUST_001, "Customer not found");
        }
        return customer.getCustomerId();
    }
}
