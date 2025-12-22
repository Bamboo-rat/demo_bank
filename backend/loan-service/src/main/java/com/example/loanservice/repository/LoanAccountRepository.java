package com.example.loanservice.repository;

import com.example.loanservice.entity.LoanAccount;
import com.example.loanservice.entity.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Loan Account
 */
@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, String> {

    /**
     * Tìm theo loan number
     */
    Optional<LoanAccount> findByLoanNumber(String loanNumber);

    /**
     * Tìm tất cả khoản vay của khách hàng
     */
    List<LoanAccount> findByCustomerId(String customerId);

    /**
     * Tìm tất cả khoản vay của khách hàng sắp xếp theo ngày tạo
     */
    List<LoanAccount> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Tìm các khoản vay của khách hàng theo danh sách trạng thái
     */
    List<LoanAccount> findByCustomerIdAndStatusIn(String customerId, List<LoanStatus> statuses);

    /**
     * Tìm khoản vay theo trạng thái
     */
    List<LoanAccount> findByStatus(LoanStatus status);

    /**
     * Tìm khoản vay của khách hàng theo trạng thái
     */
    List<LoanAccount> findByCustomerIdAndStatus(String customerId, LoanStatus status);

    /**
     * Tìm khoản vay đang hoạt động của khách hàng
     */
    @Query("SELECT la FROM LoanAccount la " +
           "WHERE la.customerId = :customerId " +
           "AND la.status IN ('ACTIVE', 'OVERDUE') " +
           "ORDER BY la.createdAt DESC")
    List<LoanAccount> findActiveLoans(@Param("customerId") String customerId);

    /**
     * Tính tổng dư nợ của khách hàng
     */
    @Query("SELECT COALESCE(SUM(la.outstandingPrincipal), 0) " +
           "FROM LoanAccount la " +
           "WHERE la.customerId = :customerId " +
           "AND la.status IN ('ACTIVE', 'OVERDUE')")
    java.math.BigDecimal getTotalOutstandingByCustomerId(@Param("customerId") String customerId);

    /**
     * Kiểm tra khách hàng có khoản vay quá hạn không
     */
    boolean existsByCustomerIdAndStatus(String customerId, LoanStatus status);

    /**
     * Đếm số khoản vay theo trạng thái
     */
    long countByStatus(LoanStatus status);
}
