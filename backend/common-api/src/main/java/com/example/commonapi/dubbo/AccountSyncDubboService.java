package com.example.commonapi.dubbo;

import com.example.commonapi.dto.account.AccountSyncRequest;
import com.example.commonapi.dto.account.AccountSyncResult;

/**
 * Dubbo contract exposed by account-service for synchronising account metadata.
 */
public interface AccountSyncDubboService {

    AccountSyncResult syncAccountMetadata(AccountSyncRequest request);
}
