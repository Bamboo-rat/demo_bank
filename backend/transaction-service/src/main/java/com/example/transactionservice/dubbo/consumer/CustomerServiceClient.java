package com.example.transactionservice.dubbo.consumer;

import com.example.commonapi.dto.customer.CustomerBasicInfo;
import com.example.commonapi.dubbo.CustomerQueryDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceClient {

    @DubboReference(
        version = "1.0.0",
        group = "banking-services",
        timeout = 5000,
        retries = 2,
        check = false
    )
    private CustomerQueryDubboService customerQueryDubboService;

    /**
     * Get customer basic info by customer ID
     * @param customerId customer ID
     * @return customer basic info
     */
    public CustomerBasicInfo getCustomerBasicInfo(String customerId) {
        return customerQueryDubboService.getCustomerBasicInfo(customerId);
    }

    /**
     * Get customer basic info by CIF number
     * @param cifNumber CIF number from core banking
     * @return customer basic info
     */
    public CustomerBasicInfo getCustomerBasicInfoByCif(String cifNumber) {
        return customerQueryDubboService.getCustomerBasicInfoByCif(cifNumber);
    }
}
