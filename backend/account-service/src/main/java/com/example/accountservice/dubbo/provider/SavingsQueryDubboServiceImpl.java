package com.example.accountservice.dubbo.provider;

import com.example.accountservice.service.FixedSavingsAccountService;
import com.example.commonapi.dto.savings.SavingsBasicInfo;
import com.example.commonapi.dubbo.SavingsQueryDubboService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * Dubbo Provider implementation cho Savings Query Service
 */
@DubboService(version = "1.0.0", group = "banking-services")
@RequiredArgsConstructor
@Slf4j
public class SavingsQueryDubboServiceImpl implements SavingsQueryDubboService {

    private final FixedSavingsAccountService savingsService;

    @Override
    public SavingsBasicInfo getSavingsBasicInfo(String savingsAccountId) {
        log.info("[DUBBO-PROVIDER] getSavingsBasicInfo called for savingsAccountId: {}", savingsAccountId);
        
        try {
            SavingsBasicInfo info = savingsService.getSavingsBasicInfo(savingsAccountId);
            log.info("[DUBBO-PROVIDER] Successfully retrieved basic info for: {}", savingsAccountId);
            return info;
            
        } catch (Exception e) {
            log.error("[DUBBO-PROVIDER] Error getting savings basic info for: {}", savingsAccountId, e);
            throw e;
        }
    }

    @Override
    public List<SavingsBasicInfo> getCustomerSavingsAccounts(String customerId) {
        log.info("[DUBBO-PROVIDER] getCustomerSavingsAccounts called for customerId: {}", customerId);
        
        try {
            List<SavingsBasicInfo> accounts = savingsService.getCustomerSavingsBasicInfo(customerId);
            log.info("[DUBBO-PROVIDER] Found {} savings accounts for customer: {}", accounts.size(), customerId);
            return accounts;
            
        } catch (Exception e) {
            log.error("[DUBBO-PROVIDER] Error getting customer savings accounts for: {}", customerId, e);
            throw e;
        }
    }

    @Override
    public boolean isSavingsAccountActive(String savingsAccountId) {
        log.info("[DUBBO-PROVIDER] isSavingsAccountActive called for savingsAccountId: {}", savingsAccountId);
        
        try {
            boolean isActive = savingsService.isSavingsAccountActive(savingsAccountId);
            log.info("[DUBBO-PROVIDER] Savings account {} active status: {}", savingsAccountId, isActive);
            return isActive;
            
        } catch (Exception e) {
            log.error("[DUBBO-PROVIDER] Error checking savings account active status: {}", savingsAccountId, e);
            return false;
        }
    }

    @Override
    public String getTotalSavingsBalance(String customerId) {
        log.info("[DUBBO-PROVIDER] getTotalSavingsBalance called for customerId: {}", customerId);
        
        try {
            String total = savingsService.getTotalSavingsBalance(customerId);
            log.info("[DUBBO-PROVIDER] Total savings balance for customer {}: {}", customerId, total);
            return total;
            
        } catch (Exception e) {
            log.error("[DUBBO-PROVIDER] Error getting total savings balance for: {}", customerId, e);
            return "0";
        }
    }
}
