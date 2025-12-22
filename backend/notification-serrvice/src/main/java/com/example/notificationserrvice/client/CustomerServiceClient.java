package com.example.notificationserrvice.client;

import com.example.commonapi.dto.customer.CustomerBasicInfo;
import com.example.commonapi.dubbo.CustomerQueryDubboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * Client to call Customer Service via Dubbo RPC
 */
@Component
@Slf4j
public class CustomerServiceClient {

    @DubboReference(version = "1.0.0", group = "banking-services", timeout = 5000, check = false)
    private CustomerQueryDubboService customerQueryDubboService;

    /**
     * Get customer ID from auth provider ID via Dubbo RPC
     * @param authProviderId Keycloak user ID
     * @return customerId or null if not found
     */
    public String getCustomerIdByAuthProviderId(String authProviderId) {
        try {
            log.debug("Calling customer service via Dubbo to get customerId for authProviderId: {}", authProviderId);
            
            CustomerBasicInfo customerInfo = customerQueryDubboService.getCustomerBasicInfoByAuthProviderId(authProviderId);
            
            if (customerInfo != null) {
                log.debug("Got customerId: {} from customer service", customerInfo.getCustomerId());
                return customerInfo.getCustomerId();
            }

            log.warn("Customer not found for authProviderId: {}", authProviderId);
            return null;
            
        } catch (Exception e) {
            log.error("Error calling customer service via Dubbo", e);
            throw new RuntimeException("Failed to get customer ID", e);
        }
    }
}
