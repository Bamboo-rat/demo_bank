package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.request.SavingsAccountCreationRequest;
import com.example.corebankingservice.dto.request.SavingsWithdrawalRequest;

/**
 * Service quản lý vòng đời sổ tiết kiệm trong Core Banking.
 */
public interface SavingsAccountService {

    /**
     * Lưu thông tin sổ tiết kiệm do account-service mở sang Core Banking.
     * @param request dữ liệu tiết kiệm
     * @return ID sổ tiết kiệm đã được ghi nhận
     */
    String createSavingsAccount(SavingsAccountCreationRequest request);

    /**
     * Ghi nhận việc tất toán/rút trước hạn sổ tiết kiệm.
     * @param request dữ liệu rút tiền
     * @return mã giao dịch ghi nhận tại Core Banking
     */
    String withdrawSavings(SavingsWithdrawalRequest request);
}
