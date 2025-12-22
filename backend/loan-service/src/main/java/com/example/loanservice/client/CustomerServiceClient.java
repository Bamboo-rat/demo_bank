package com.example.loanservice.client;

import com.example.loanservice.dto.customer.CustomerInfoResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceClient {
    
    @DubboReference(version = "1.0.0", timeout = 5000, check = false)
    private CustomerService customerService;
    
    /**
     * Get customer information via Dubbo
     */
    public CustomerInfoResponse getCustomerInfo(String cifId) {
        return customerService.getCustomerInfo(cifId);
    }
    
    /**
     * Verify customer is active and KYC completed
     */
    public boolean verifyCustomer(String cifId) {
        CustomerInfoResponse customer = customerService.getCustomerInfo(cifId);
        return customer != null && 
               "ACTIVE".equals(customer.getStatus()) && 
               customer.isKycCompleted();
    }
    
    /**
     * Dubbo service interface - should be in common-api or separate client module
     */
    public interface CustomerService {
        CustomerInfoResponse getCustomerInfo(String cifId);
    }
}
