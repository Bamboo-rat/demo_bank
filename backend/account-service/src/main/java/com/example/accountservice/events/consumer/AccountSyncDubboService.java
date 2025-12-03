package com.example.accountservice.events.consumer;

import com.example.accountservice.dto.dubbo.AccountSyncRequest;
import com.example.accountservice.dto.dubbo.AccountSyncResult;

/**
 * Dubbo RPC service for syncing account metadata from CustomerService
 */
public interface AccountSyncDubboService {

    /**
     * Sync account metadata when account is created in CoreBank
     * @param request Account sync request containing metadata
     * @return Sync result
     */
    AccountSyncResult syncAccountMetadata(AccountSyncRequest request);
}
