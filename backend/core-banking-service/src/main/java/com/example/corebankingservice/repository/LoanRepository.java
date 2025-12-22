package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.Loan;
import com.example.corebankingservice.entity.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho Core Banking Loan
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {

    /**
     * Tìm khoản vay theo reference từ Loan Service
     */
    Optional<Loan> findByLoanServiceRef(String loanServiceRef);

    /**
     * Tìm tất cả khoản vay của khách hàng
     */
    List<Loan> findByCifIdOrderByCreatedAtDesc(String cifId);

    /**
     * Tìm khoản vay theo tài khoản
     */
    List<Loan> findByAccountId(String accountId);

    /**
     * Tìm khoản vay theo trạng thái
     */
    List<Loan> findByStatus(LoanStatus status);

    /**
     * Tìm khoản vay của khách hàng theo trạng thái
     */
    List<Loan> findByCifIdAndStatus(String cifId, LoanStatus status);

    /**
     * Tính tổng dư nợ của khách hàng
     */
    @Query("SELECT COALESCE(SUM(l.outstandingPrincipal), 0) " +
           "FROM Loan l " +
           "WHERE l.cifId = :cifId " +
           "AND l.status IN ('ACTIVE', 'OVERDUE')")
    BigDecimal getTotalOutstandingByCifId(@Param("cifId") String cifId);

    /**
     * Kiểm tra khách hàng có khoản vay quá hạn không
     */
    boolean existsByCifIdAndStatus(String cifId, LoanStatus status);
}
