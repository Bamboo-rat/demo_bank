package com.example.customerservice.events.consumer;

import com.example.accountservice.dto.dubbo.AccountSyncRequest;
import com.example.accountservice.dto.dubbo.AccountSyncResult;
import com.example.accountservice.events.producer.AccountSyncDubboProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * Dubbo consumer - calls AccountService to sync account metadata
 * Customer-service consumes the Dubbo service provided by Account-service
 */
@Component
@Slf4j
public class AccountSyncConsumer {

    @DubboReference(version = "1.0.0", group = "banking-services", check = false, timeout = 5000)
    private AccountSyncDubboProducer accountSyncDubboProducer;

    /**
     * Sync account metadata to account-service via Dubbo RPC
     * 
     * @param request Account sync request with metadata
     * @return Result with success status and accountId
     */
    public AccountSyncResult syncAccountMetadata(AccountSyncRequest request) {
        log.info("Syncing account metadata via Dubbo for accountNumber: {}", request.getAccountNumber());
        
        try {
            AccountSyncResult result = accountSyncDubboProducer.syncAccountMetadata(request);
            
            if (result.isSuccess()) {
                log.info("Successfully synced account metadata: accountNumber={}, accountId={}", 
                    request.getAccountNumber(), result.getAccountId());
            } else {
                log.warn("Account sync returned success=false: accountNumber={}, message={}", 
                    request.getAccountNumber(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to sync account metadata via Dubbo: accountNumber={}", 
                request.getAccountNumber(), e);
            throw new RuntimeException("Dubbo account sync failed", e);
        }
    }
}
