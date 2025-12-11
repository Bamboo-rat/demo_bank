package com.example.transactionservice.service.impl;

import com.example.transactionservice.dto.response.BankResponse;
import com.example.transactionservice.dto.response.VietQRBankListResponse;
import com.example.transactionservice.service.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceImpl implements BankService {

    private static final String VIETQR_API_URL = "https://api.vietqr.io/v2/banks";
    private final RestTemplate restTemplate;

    @Override
    @Cacheable(value = "banks", unless = "#result == null || #result.isEmpty()")
    public List<BankResponse> getAllBanks() {
        try {
            log.info("Fetching bank list from VietQR API");
            VietQRBankListResponse response = restTemplate.getForObject(
                    VIETQR_API_URL,
                    VietQRBankListResponse.class
            );

            if (response != null && "00".equals(response.getCode())) {
                log.info("Successfully fetched {} banks", response.getData().size());
                return response.getData();
            }

            log.warn("Failed to fetch banks. Response code: {}", response != null ? response.getCode() : "null");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching bank list from VietQR API", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<BankResponse> searchBanks(String searchTerm) {
        List<BankResponse> allBanks = getAllBanks();
        
        if (searchTerm == null || searchTerm.isBlank()) {
            return allBanks;
        }

        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        
        return allBanks.stream()
                .filter(bank -> 
                    bank.getName().toLowerCase().contains(lowerSearchTerm) ||
                    bank.getShortName().toLowerCase().contains(lowerSearchTerm) ||
                    bank.getCode().toLowerCase().contains(lowerSearchTerm)
                )
                .collect(Collectors.toList());
    }

    @Override
    public BankResponse getBankByCode(String bankCode) {
        return getAllBanks().stream()
                .filter(bank -> bank.getCode().equalsIgnoreCase(bankCode))
                .findFirst()
                .orElse(null);
    }

    @Override
    public BankResponse getBankByBin(String bin) {
        return getAllBanks().stream()
                .filter(bank -> bank.getBin().equals(bin))
                .findFirst()
                .orElse(null);
    }
}
