package com.example.commonapi.dubbo;

import com.example.commonapi.dto.customer.CustomerBasicInfo;

public interface CustomerQueryDubboService {

    /**
     * Get customer basic info by customer ID
     * @param customerId UUID of the customer
     * @return CustomerBasicInfo or null if not found
     */
    CustomerBasicInfo getCustomerBasicInfo(String customerId);

    /**
     * Get customer basic info by CIF number
     * @param cifNumber CIF number from core banking
     * @return CustomerBasicInfo or null if not found
     */
    CustomerBasicInfo getCustomerBasicInfoByCif(String cifNumber);
    
    /**
     * Get customer basic info by auth provider ID (Keycloak ID)
     * @param authProviderId Keycloak user ID
     * @return CustomerBasicInfo or null if not found
     */
    CustomerBasicInfo getCustomerBasicInfoByAuthProviderId(String authProviderId);
}
