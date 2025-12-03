package com.example.accountservice.service;

import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse getAccountDetails(String accountNumber);
    List<AccountResponse> getAccountsByCustomerId(String customerId);
    boolean isAccountActive(String accountNumber);
    AccountResponse updateAccount(String accountNumber, String customerId, UpdateAccountRequest request);
}
