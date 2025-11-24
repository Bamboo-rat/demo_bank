package com.example.customerservice.service;

import com.example.customerservice.dto.request.CustomerLoginDTO;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.request.CustomerUpdateRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.entity.enums.KycStatus;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRegisterRequest registerDto);
    Object loginCustomer(CustomerLoginDTO loginDto);
    CustomerResponse getMyInfo();
    CustomerResponse getCustomerById(String customerId);
    CustomerResponse updateCustomer(String authProviderId, CustomerUpdateRequest updateRequest);
    void updateKycStatus(String nationalId, KycStatus kycStatus);
}
