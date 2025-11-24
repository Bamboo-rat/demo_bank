package com.example.customerservice.service;

import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.EkycResponse;

public interface EkycService {
    EkycResponse verifyUser(CustomerRegisterRequest cus);
}
