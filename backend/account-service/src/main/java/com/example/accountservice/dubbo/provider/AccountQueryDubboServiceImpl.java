package com.example.accountservice.dubbo.provider;

import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.service.AccountService;
import com.example.commonapi.dto.account.AccountInfoDTO;
import com.example.commonapi.dubbo.AccountQueryDubboService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Dubbo Service Provider for Account Query Operations
 * Exposes account query methods via Dubbo RPC
 */
@Slf4j
@DubboService(version = "1.0.0", group = "banking-services")
@RequiredArgsConstructor
public class AccountQueryDubboServiceImpl implements AccountQueryDubboService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountInfoDTO getAccountInfoByNumber(String accountNumber) {
        log.info("Dubbo call - Getting account info for: {}", accountNumber);
        
        // Use existing service method
        com.example.accountservice.dto.response.AccountInfoDTO serviceDTO = 
            accountService.getAccountInfoByNumber(accountNumber);
        
        // Map to common-api DTO
        return mapToCommonDTO(serviceDTO);
    }

    @Override
    public boolean isAccountActive(String accountNumber) {
        log.info("Dubbo call - Checking if account is active: {}", accountNumber);
        return accountService.isAccountActive(accountNumber);
    }

    /**
     * Map service DTO to common-api DTO
     */
    private AccountInfoDTO mapToCommonDTO(com.example.accountservice.dto.response.AccountInfoDTO serviceDTO) {
        if (serviceDTO == null) {
            return null;
        }
        
        return AccountInfoDTO.builder()
            .accountNumber(serviceDTO.getAccountNumber())
            .accountHolderName(serviceDTO.getAccountHolderName())
            .accountType(serviceDTO.getAccountType() != null ? serviceDTO.getAccountType().name() : null)
            .status(serviceDTO.getStatus() != null ? serviceDTO.getStatus().name() : null)
            .bankName(serviceDTO.getBankName())
            .bankCode(serviceDTO.getBankCode())
            .cifNumber(serviceDTO.getCifNumber())
            .isActive(serviceDTO.getIsActive())
            .build();
    }
}
