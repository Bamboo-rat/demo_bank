package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.response.PartnerBankAccountResponse;
import com.example.corebankingservice.service.PartnerBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerBankServiceImpl implements PartnerBankService {

    private final RestTemplate restTemplate;

    @Value("${partner.bank.base-url:http://localhost:8090}")
    private String partnerBankBaseUrl;

    @Override
    public PartnerBankAccountResponse verifyAccount(String bankCode, String accountNumber) {
        try {
            String url = String.format("%s/api/partner/%s/verify-account", 
                    partnerBankBaseUrl, bankCode.toLowerCase());
            
            Map<String, String> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            
            log.info("Verifying account {} at bank {}", accountNumber, bankCode);
            
            PartnerBankAccountResponse response = restTemplate.postForObject(
                    url,
                    request,
                    PartnerBankAccountResponse.class
            );
            
            log.info("Account verification result: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error verifying account at partner bank", e);
            return PartnerBankAccountResponse.builder()
                    .accountNumber(accountNumber)
                    .bankCode(bankCode)
                    .exists(false)
                    .active(false)
                    .message("Unable to verify account: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getAccountName(String bankCode, String accountNumber) {
        PartnerBankAccountResponse response = verifyAccount(bankCode, accountNumber);
        
        if (response != null && response.getExists()) {
            return response.getAccountName();
        }
        
        return null;
    }
}
