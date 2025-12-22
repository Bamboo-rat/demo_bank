package com.example.commonapi.dubbo;

import com.example.commonapi.dto.savings.SavingsBasicInfo;

import java.util.List;

/**
 * Dubbo service interface cho truy vấn Savings Account
 */
public interface SavingsQueryDubboService {

    /**
     * Lấy thông tin cơ bản của savings account theo ID
     */
    SavingsBasicInfo getSavingsBasicInfo(String savingsAccountId);

    /**
     * Lấy danh sách savings accounts của khách hàng
     */
    List<SavingsBasicInfo> getCustomerSavingsAccounts(String customerId);

    /**
     * Kiểm tra savings account có đang active không
     */
    boolean isSavingsAccountActive(String savingsAccountId);

    /**
     * Lấy tổng số dư tiết kiệm của khách hàng
     */
    String getTotalSavingsBalance(String customerId);
}
