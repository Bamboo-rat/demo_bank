package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.request.AccountLifecycleActionRequest;
import com.example.corebankingservice.dto.request.AccountStatusUpdateRequest;
import com.example.corebankingservice.dto.request.OpenAccountCoreRequest;
import com.example.corebankingservice.dto.response.AccountDetailResponse;
import com.example.corebankingservice.dto.response.AccountStatusHistoryResponse;
import com.example.corebankingservice.dto.response.AccountStatusResponse;

import java.util.List;

public interface AccountLifecycleService {

    AccountDetailResponse openAccount(OpenAccountCoreRequest request);

    AccountDetailResponse closeAccount(String accountNumber, AccountLifecycleActionRequest request);

    AccountDetailResponse freezeAccount(String accountNumber, AccountLifecycleActionRequest request);

    AccountDetailResponse unfreezeAccount(String accountNumber, AccountLifecycleActionRequest request);

    AccountDetailResponse blockAccount(String accountNumber, AccountLifecycleActionRequest request);

    AccountDetailResponse unblockAccount(String accountNumber, AccountLifecycleActionRequest request);

    AccountStatusResponse getAccountStatus(String accountNumber);

    AccountDetailResponse updateAccountStatus(String accountNumber, AccountStatusUpdateRequest request);

    AccountDetailResponse getAccountDetail(String accountNumber);

    List<AccountStatusHistoryResponse> getStatusHistory(String accountNumber);
}
