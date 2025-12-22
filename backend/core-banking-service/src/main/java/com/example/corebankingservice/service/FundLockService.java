package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.LockFundsRequest;
import com.example.corebankingservice.dto.LockFundsResponse;
import com.example.corebankingservice.dto.UnlockFundsRequest;

/**
 * Service để quản lý việc lock/unlock tiền trong tài khoản
 */
public interface FundLockService {
    
    /**
     * Lock tiền trong tài khoản
     * @param request Lock request
     * @return Lock response với lock ID
     */
    LockFundsResponse lockFunds(LockFundsRequest request);
    
    /**
     * Unlock/release tiền đã lock
     * @param request Unlock request
     * @return Lock response sau khi unlock
     */
    LockFundsResponse unlockFunds(UnlockFundsRequest request);
    
    /**
     * Unlock funds theo reference ID (ví dụ: savings account ID)
     * @param referenceId Reference ID
     * @param reason Lý do unlock
     * @return Lock response sau khi unlock
     */
    LockFundsResponse unlockFundsByReference(String referenceId, String reason);
}
