package com.example.accountservice.events.producer;

import com.example.accountservice.dto.dubbo.AccountSyncRequest;
import com.example.accountservice.dto.dubbo.AccountSyncResult;

/**
 * Dubbo service producer - Account-service provides this service
 * CustomerService consumes this service to sync account metadata
 */
public interface AccountSyncDubboProducer {

    /**
     * Sync account metadata when account is created in CoreBank
     * @param request Account sync request containing metadata
     * @return Sync result
     */
    AccountSyncResult syncAccountMetadata(AccountSyncRequest request);
}
