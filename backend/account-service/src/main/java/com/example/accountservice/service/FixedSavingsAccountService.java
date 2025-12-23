package com.example.accountservice.service;

import com.example.accountservice.dto.savings.*;
import com.example.commonapi.dto.savings.SavingsBasicInfo;

import java.util.List;

/**
 * Service interface cho Fixed Savings Account
 */
public interface FixedSavingsAccountService {

    /**
     * Tính toán và xem trước thông tin tiết kiệm trước khi mở sổ
     * 
     * @param request Thông tin preview
     * @return Thông tin dự kiến về lãi suất, tiền lãi, ngày đáo hạn
     */
    SavingsPreviewResponse calculatePreview(SavingsPreviewRequest request);

    /**
     * Lấy danh sách các sản phẩm tiết kiệm có sẵn
     * 
     * @return Danh sách sản phẩm tiết kiệm
     */
    List<SavingsProductResponse> getSavingsProducts();

    /**
     * Mở sổ tiết kiệm kỳ hạn mới
     * 
     * @param request Thông tin mở sổ
     * @param customerId UUID của khách hàng
     * @return Thông tin chi tiết sổ tiết kiệm vừa mở
     */
    SavingsAccountResponse openSavingsAccount(OpenSavingsRequest request, String customerId);

    /**
     * Lấy thông tin chi tiết sổ tiết kiệm theo ID
     * 
     * @param savingsAccountId ID của sổ tiết kiệm
     * @param customerId UUID của khách hàng (để check quyền)
     * @return Thông tin chi tiết
     */
    SavingsAccountResponse getSavingsAccountById(String savingsAccountId, String customerId);

    /**
     * Lấy danh sách tất cả sổ tiết kiệm của khách hàng
     * 
     * @param customerId UUID của khách hàng
     * @return Danh sách sổ tiết kiệm
     */
    List<SavingsAccountResponse> getCustomerSavingsAccounts(String customerId);

    /**
     * Rút tiền trước hạn (mất lãi ưu đãi)
     * 
     * @param savingsAccountId ID của sổ tiết kiệm
     * @param customerId UUID của khách hàng (để check quyền)
     * @return Thông tin rút tiền và số tiền thực nhận
     */
    PrematureWithdrawResponse prematureWithdraw(String savingsAccountId, String customerId);

    /**
     * Lấy thông tin cơ bản cho Dubbo service (không check quyền)
     * 
     * @param savingsAccountId ID của sổ tiết kiệm
     * @return Thông tin cơ bản
     */
    SavingsBasicInfo getSavingsBasicInfo(String savingsAccountId);

    /**
     * Lấy danh sách thông tin cơ bản cho Dubbo service
     * 
     * @param customerId UUID của khách hàng
     * @return Danh sách thông tin cơ bản
     */
    List<SavingsBasicInfo> getCustomerSavingsBasicInfo(String customerId);

    /**
     * Kiểm tra savings account có đang active không
     * 
     * @param savingsAccountId ID của sổ tiết kiệm
     * @return true nếu active, false nếu không
     */
    boolean isSavingsAccountActive(String savingsAccountId);

    /**
     * Lấy tổng số dư tiết kiệm của khách hàng
     * 
     * @param customerId UUID của khách hàng
     * @return Tổng số dư (chỉ tính ACTIVE)
     */
    String getTotalSavingsBalance(String customerId);
}
