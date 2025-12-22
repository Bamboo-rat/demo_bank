package com.example.customerservice.dubbo.provider;

import com.example.commonapi.dto.customer.CustomerBasicInfo;
import com.example.commonapi.dubbo.CustomerQueryDubboService;
import com.example.customerservice.entity.Customer;
import com.example.customerservice.mapper.CustomerMapper;
import com.example.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Dubbo service implementation for customer queries
 * Provides customer information to other services via Dubbo RPC
 */
@DubboService(version = "1.0.0", group = "banking-services", timeout = 5000)
@RequiredArgsConstructor
@Slf4j
public class CustomerQueryDubboServiceImpl implements CustomerQueryDubboService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerBasicInfo getCustomerBasicInfo(String customerId) {
        log.info("Dubbo call: getCustomerBasicInfo for customerId: {}", customerId);
        
        if (customerId == null || customerId.isBlank()) {
            log.warn("Invalid customerId: {}", customerId);
            return null;
        }

        try {
            return customerRepository.findById(customerId)
                    .map(this::mapToBasicInfo)
                    .orElseGet(() -> {
                        log.warn("Customer not found: {}", customerId);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error fetching customer info for customerId: {}", customerId, e);
            return null;
        }
    }

    @Override
    public CustomerBasicInfo getCustomerBasicInfoByCif(String cifNumber) {
        log.info("Dubbo call: getCustomerBasicInfoByCif for cifNumber: {}", cifNumber);
        
        if (cifNumber == null || cifNumber.isBlank()) {
            log.warn("Invalid cifNumber: {}", cifNumber);
            return null;
        }

        try {
            return customerRepository.findByCifNumber(cifNumber)
                    .map(this::mapToBasicInfo)
                    .orElseGet(() -> {
                        log.warn("Customer not found for cifNumber: {}", cifNumber);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error fetching customer info for cifNumber: {}", cifNumber, e);
            return null;
        }
    }

    @Override
    public CustomerBasicInfo getCustomerBasicInfoByAuthProviderId(String authProviderId) {
        log.info("Dubbo call: getCustomerBasicInfoByAuthProviderId for authProviderId: {}", authProviderId);
        
        if (authProviderId == null || authProviderId.isBlank()) {
            log.warn("Invalid authProviderId: {}", authProviderId);
            return null;
        }

        try {
            return customerRepository.findByAuthProviderId(authProviderId)
                    .map(this::mapToBasicInfo)
                    .orElseGet(() -> {
                        log.warn("Customer not found for authProviderId: {}", authProviderId);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error fetching customer info for authProviderId: {}", authProviderId, e);
            return null;
        }
    }

    private CustomerBasicInfo mapToBasicInfo(Customer customer) {
        return customerMapper.toBasicInfo(customer);
    }
}
