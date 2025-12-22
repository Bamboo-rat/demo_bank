package com.example.loanservice.repository;

import com.example.loanservice.entity.LoanPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho Loan Payment History
 */
@Repository
public interface LoanPaymentHistoryRepository extends JpaRepository<LoanPaymentHistory, String> {

    /**
     * Tìm lịch sử thanh toán của một khoản vay (order by createdAt)
     */
    List<LoanPaymentHistory> findByLoanIdOrderByCreatedAtDesc(String loanId);

    /**
     * Tìm lịch sử thanh toán của một khoản vay (order by paidDate)
     */
    List<LoanPaymentHistory> findByLoanIdOrderByPaidDateDesc(String loanId);

    /**
     * Tìm lịch sử thanh toán của một kỳ trả nợ
     */
    List<LoanPaymentHistory> findByScheduleIdOrderByPaidDateDesc(String scheduleId);

    /**
     * Tìm lịch sử thanh toán theo Core Banking transaction reference
     */
    List<LoanPaymentHistory> findByCoreTxRef(String coreTxRef);

    /**
     * Tìm lịch sử thanh toán trong khoảng thời gian
     */
    @Query("SELECT lph FROM LoanPaymentHistory lph " +
           "WHERE lph.loanId = :loanId " +
           "AND lph.paidDate BETWEEN :startDate AND :endDate " +
           "ORDER BY lph.paidDate DESC")
    List<LoanPaymentHistory> findByLoanIdAndDateRange(
           @Param("loanId") String loanId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng số tiền đã trả của khoản vay
     */
    @Query("SELECT COALESCE(SUM(lph.paidAmount), 0) " +
           "FROM LoanPaymentHistory lph " +
           "WHERE lph.loanId = :loanId " +
           "AND lph.result = 'SUCCESS'")
    java.math.BigDecimal getTotalPaidAmount(@Param("loanId") String loanId);

    /**
     * Đếm số lần thanh toán thành công
     */
       long countByLoanIdAndResult(String loanId, String result);
}
