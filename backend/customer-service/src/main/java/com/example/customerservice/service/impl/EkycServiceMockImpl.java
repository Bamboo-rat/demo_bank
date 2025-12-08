package com.example.customerservice.service.impl;

import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.EkycResponse;
import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.service.CustomerService;
import com.example.customerservice.service.EkycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EkycServiceMockImpl implements EkycService {

    private final CustomerService customerService;

    @Override
    public EkycResponse verifyUser(CustomerRegisterRequest customerRegisterDTO) {
        // Giả lập một chút độ trễ
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    
        EkycResponse response = EkycResponse.builder()
                .status("SUCCESS")
                .message("eKYC verification completed successfully")
                .fullName(customerRegisterDTO.getFullName())
                .dateOfBirth(customerRegisterDTO.getDateOfBirth())
                .nationalId(customerRegisterDTO.getNationalId())
                .verified(true) 
                .build();

        log.info("eKYC simulation completed successfully for national ID: {}", customerRegisterDTO.getNationalId());

        return response;
    }
}
