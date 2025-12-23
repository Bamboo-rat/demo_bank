package com.example.loanservice.client;

import com.example.commonapi.dto.customer.CustomerBasicInfo;
import com.example.commonapi.dubbo.CustomerQueryDubboService;
import com.example.loanservice.dto.customer.CustomerInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerServiceClient {

    @DubboReference(version = "1.0.0", group = "banking-services", timeout = 5000, check = false)
    private CustomerQueryDubboService customerQueryDubboService;

    /**
     * Resolve customer by platform auth provider ID (Keycloak subject)
     */
    public CustomerInfoResponse getCustomerInfoByAuthProviderId(String authProviderId) {
        CustomerBasicInfo info = customerQueryDubboService.getCustomerBasicInfoByAuthProviderId(authProviderId);
        return mapToResponse(info);
    }

    /**
     * Resolve customer by internal customerId (UUID stored in services)
     */
    public CustomerInfoResponse getCustomerInfoByCustomerId(String customerId) {
        CustomerBasicInfo info = customerQueryDubboService.getCustomerBasicInfo(customerId);
        return mapToResponse(info);
    }

    public boolean verifyCustomer(String authProviderId) {
        CustomerInfoResponse customer = getCustomerInfoByAuthProviderId(authProviderId);
        return customer != null && customer.isKycCompleted();
    }

    private CustomerInfoResponse mapToResponse(CustomerBasicInfo info) {
        if (info == null) {
            log.warn("Customer info not found via Dubbo response");
            return null;
        }

        return CustomerInfoResponse.builder()
                .customerId(info.getCustomerId())
                .authProviderId(info.getAuthProviderId())
                .cifId(info.getCifNumber())
                .fullName(info.getFullName())
                .email(info.getEmail())
                .phone(info.getPhoneNumber())
                .status(info.getCustomerStatus())
                .kycCompleted("VERIFIED".equalsIgnoreCase(info.getCustomerStatus()))
                .build();
    }
}
