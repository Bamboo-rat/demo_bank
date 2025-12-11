package com.example.accountservice.repository;

import com.example.accountservice.entity.Beneficiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {

    /**
     * Find all beneficiaries for a customer
     */
    List<Beneficiary> findByCustomerIdOrderByLastTransferDateDesc(String customerId);

    /**
     * Find all beneficiaries for a customer with pagination
     */
    Page<Beneficiary> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find beneficiary by customer and account number
     */
    Optional<Beneficiary> findByCustomerIdAndBeneficiaryAccountNumber(String customerId, String beneficiaryAccountNumber);

    /**
     * Check if beneficiary exists for customer
     */
    boolean existsByCustomerIdAndBeneficiaryAccountNumber(String customerId, String beneficiaryAccountNumber);

    /**
     * Find beneficiaries by customer and bank code (for inter-bank transfers)
     */
    List<Beneficiary> findByCustomerIdAndBankCode(String customerId, String bankCode);

    /**
     * Find internal bank beneficiaries (bankCode is null)
     */
    List<Beneficiary> findByCustomerIdAndBankCodeIsNull(String customerId);

    /**
     * Search beneficiaries by name or nickname
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.customerId = :customerId " +
           "AND (LOWER(b.beneficiaryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(b.nickname) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Beneficiary> searchByNameOrNickname(@Param("customerId") String customerId, 
                                             @Param("searchTerm") String searchTerm);

    /**
     * Update transfer statistics
     */
    @Modifying
    @Query("UPDATE Beneficiary b SET b.transferCount = b.transferCount + 1, " +
           "b.lastTransferDate = :transferDate WHERE b.beneficiaryId = :beneficiaryId")
    void updateTransferStats(@Param("beneficiaryId") String beneficiaryId, 
                            @Param("transferDate") LocalDateTime transferDate);

    /**
     * Get most frequently used beneficiaries
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.customerId = :customerId " +
           "ORDER BY b.transferCount DESC, b.lastTransferDate DESC")
    List<Beneficiary> findMostUsedBeneficiaries(@Param("customerId") String customerId, Pageable pageable);

    /**
     * Count beneficiaries for a customer
     */
    long countByCustomerId(String customerId);
}
