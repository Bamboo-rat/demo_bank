package com.example.loanservice.repository;

import com.example.loanservice.entity.LoanApplication;
import com.example.loanservice.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Loan Application
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    /**
     * Tìm tất cả đơn vay của khách hàng
     */
    List<LoanApplication> findByCustomerId(String customerId);

    /**
     * Tìm tất cả đơn vay của khách hàng sắp xếp theo ngày tạo
     */
    List<LoanApplication> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Tìm đơn vay theo trạng thái
     */
    List<LoanApplication> findByStatus(ApplicationStatus status);

    /**
     * Tìm đơn vay của khách hàng theo trạng thái
     */
    List<LoanApplication> findByCustomerIdAndStatus(String customerId, ApplicationStatus status);

    /**
     * Kiểm tra khách hàng có đơn vay đang chờ duyệt không
     */
    @Query("SELECT CASE WHEN COUNT(la) > 0 THEN true ELSE false END " +
           "FROM LoanApplication la " +
           "WHERE la.customerId = :customerId " +
           "AND la.status IN ('PENDING_APPROVAL', 'UNDER_REVIEW')")
    boolean existsPendingApplicationByCustomerId(@Param("customerId") String customerId);

    /**
     * Đếm số đơn vay theo trạng thái
     */
    long countByStatus(ApplicationStatus status);
}
