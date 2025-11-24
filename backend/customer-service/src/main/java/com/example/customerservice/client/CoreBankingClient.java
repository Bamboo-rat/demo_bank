package com.example.customerservice.client;

import com.example.customerservice.dto.request.CreateCoreCustomerRequest;
import com.example.customerservice.dto.response.CreateCoreCustomerResponse;

public interface CoreBankingClient {
    CreateCoreCustomerResponse createCoreCustomer(CreateCoreCustomerRequest request);
    void deleteCoreCustomer(String coreCustomerId);
}