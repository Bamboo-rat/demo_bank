package com.example.accountservice.repository;

import com.example.accountservice.entity.FixedSavingsAccount;
import com.example.accountservice.entity.enums.SavingsAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FixedSavingsAccountRepository extends JpaRepository<FixedSavingsAccount, String> {

    /**
     * Lấy tất cả savings account của khách hàng theo status
     */
    List<FixedSavingsAccount> findByCustomerIdAndStatus(String customerId, SavingsAccountStatus status);

    /**
     * Lấy tất cả savings account của khách hàng
     */
    List<FixedSavingsAccount> findByCustomerId(String customerId);

    /**
     * Lấy savings account theo customerId và savingsAccountId (để check quyền)
     */
    Optional<FixedSavingsAccount> findByCustomerIdAndSavingsAccountId(String customerId, String savingsAccountId);

    /**
     * Tìm tất cả savings account đã đáo hạn và chưa xử lý
     * Dùng cho scheduler job chạy hàng ngày
     */
    List<FixedSavingsAccount> findAllByMaturityDateBeforeAndStatus(LocalDate date, SavingsAccountStatus status);

    /**
     * Tính tổng số dư tiết kiệm của khách hàng (chỉ tính ACTIVE)
     */
    @Query("SELECT COALESCE(SUM(f.principalAmount), 0) FROM FixedSavingsAccount f " +
           "WHERE f.customerId = :customerId AND f.status = 'ACTIVE'")
    String getTotalSavingsBalance(@Param("customerId") String customerId);

    /**
     * Kiểm tra khách hàng có savings account nào đang ACTIVE không
     */
    boolean existsByCustomerIdAndStatus(String customerId, SavingsAccountStatus status);

    /**
     * Đếm số lượng savings account active của khách hàng
     */
    long countByCustomerIdAndStatus(String customerId, SavingsAccountStatus status);
}
