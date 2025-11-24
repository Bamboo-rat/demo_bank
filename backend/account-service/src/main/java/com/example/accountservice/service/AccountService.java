package com.example.accountservice.service;

import com.example.accountservice.dto.request.OpenAccountRequest;
import com.example.accountservice.dto.request.UpdateAccountRequest;
import com.example.accountservice.dto.response.AccountResponse;

import java.util.List;

/**
 * Local Account Service interface for internal operations
 * Uses local DTOs
 */
public interface AccountService {

    AccountResponse openAccount(OpenAccountRequest request);
    AccountResponse getAccountDetails(String accountNumber);
    List<AccountResponse> getAccountsByCustomerId(String customerId);
    void closeAccount(String accountNumber, String customerId);
    boolean isAccountActive(String accountNumber);
    AccountResponse updateAccount(String accountNumber, String customerId, UpdateAccountRequest request);
}
