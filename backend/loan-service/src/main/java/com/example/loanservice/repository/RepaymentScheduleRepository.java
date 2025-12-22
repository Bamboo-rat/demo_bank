package com.example.loanservice.repository;

import com.example.loanservice.entity.RepaymentSchedule;
import com.example.loanservice.entity.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho Repayment Schedule
 */
@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, String> {

    /**
     * Tìm tất cả lịch trả nợ của một khoản vay
     */
    List<RepaymentSchedule> findByLoanIdOrderByInstallmentNoAsc(String loanId);

    /**
     * Tìm lịch trả nợ theo kỳ
     */
    Optional<RepaymentSchedule> findByLoanIdAndInstallmentNo(String loanId, Integer installmentNo);

    /**
     * Tìm kỳ trả nợ tiếp theo
     */
    @Query("SELECT rs FROM RepaymentSchedule rs " +
           "WHERE rs.loanId = :loanId " +
           "AND rs.status = 'PENDING' " +
           "ORDER BY rs.installmentNo ASC")
    Optional<RepaymentSchedule> findNextPendingInstallment(@Param("loanId") String loanId);

    /**
     * Tìm các kỳ trả nợ quá hạn
     */
    @Query("SELECT rs FROM RepaymentSchedule rs " +
           "WHERE rs.status = 'PENDING' " +
           "AND rs.dueDate < :currentDate")
    List<RepaymentSchedule> findOverdueInstallments(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm các kỳ trả nợ sắp đến hạn (T-3, T-1)
     */
    @Query("SELECT rs FROM RepaymentSchedule rs " +
           "WHERE rs.status = 'PENDING' " +
           "AND rs.dueDate BETWEEN :startDate AND :endDate")
    List<RepaymentSchedule> findUpcomingInstallments(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Đếm số kỳ đã trả của khoản vay
     */
    long countByLoanIdAndStatus(String loanId, InstallmentStatus status);

    /**
     * Tìm các kỳ trả nợ theo trạng thái
     */
    List<RepaymentSchedule> findByLoanIdAndStatus(String loanId, InstallmentStatus status);

    /**
     * Tìm các kỳ trả nợ theo trạng thái và sắp xếp
     */
    List<RepaymentSchedule> findByLoanIdAndStatusOrderByInstallmentNoAsc(String loanId, InstallmentStatus status);

    /**
     * Tìm kỳ trả nợ đầu tiên theo trạng thái
     */
    Optional<RepaymentSchedule> findFirstByLoanIdAndStatusOrderByInstallmentNoAsc(String loanId, InstallmentStatus status);

    /**
     * Tìm các kỳ trả nợ theo danh sách trạng thái
     */
    List<RepaymentSchedule> findByLoanIdAndStatusInOrderByInstallmentNoAsc(String loanId, List<InstallmentStatus> statuses);

    /**
     * Tìm các kỳ trả nợ theo trạng thái và khoảng thời gian đến hạn
     */
    List<RepaymentSchedule> findByStatusAndDueDateBetween(InstallmentStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * Tìm các kỳ trả nợ theo trạng thái và đến hạn trước ngày chỉ định
     */
    List<RepaymentSchedule> findByStatusAndDueDateBefore(InstallmentStatus status, LocalDate date);

    /**
     * Tính tổng số tiền còn phải trả của khoản vay
     */
    @Query("SELECT COALESCE(SUM(rs.totalAmount - COALESCE(rs.paidAmount, 0)), 0) " +
           "FROM RepaymentSchedule rs " +
           "WHERE rs.loanId = :loanId " +
           "AND rs.status IN ('PENDING', 'OVERDUE')")
    java.math.BigDecimal getTotalRemainingAmount(@Param("loanId") String loanId);
}
