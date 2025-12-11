package com.example.accountservice.service;

import com.example.accountservice.dto.request.CreateBeneficiaryRequest;
import com.example.accountservice.dto.request.UpdateBeneficiaryRequest;
import com.example.accountservice.dto.response.BeneficiaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing beneficiaries (danh bạ thụ hưởng)
 */
public interface BeneficiaryService {

    /**
     * Create a new beneficiary
     * @param customerId the customer ID who owns this beneficiary
     * @param request the beneficiary details
     * @return created beneficiary
     */
    BeneficiaryResponse createBeneficiary(String customerId, CreateBeneficiaryRequest request);

    /**
     * Update beneficiary details
     * @param customerId the customer ID
     * @param beneficiaryId the beneficiary ID
     * @param request the update details
     * @return updated beneficiary
     */
    BeneficiaryResponse updateBeneficiary(String customerId, String beneficiaryId, UpdateBeneficiaryRequest request);

    /**
     * Delete a beneficiary
     * @param customerId the customer ID
     * @param beneficiaryId the beneficiary ID
     */
    void deleteBeneficiary(String customerId, String beneficiaryId);

    /**
     * Get beneficiary by ID
     * @param customerId the customer ID
     * @param beneficiaryId the beneficiary ID
     * @return beneficiary details
     */
    BeneficiaryResponse getBeneficiary(String customerId, String beneficiaryId);

    /**
     * Get all beneficiaries for a customer
     * @param customerId the customer ID
     * @return list of beneficiaries
     */
    List<BeneficiaryResponse> getAllBeneficiaries(String customerId);

    /**
     * Get beneficiaries with pagination
     * @param customerId the customer ID
     * @param pageable pagination parameters
     * @return page of beneficiaries
     */
    Page<BeneficiaryResponse> getBeneficiaries(String customerId, Pageable pageable);

    /**
     * Search beneficiaries by name or nickname
     * @param customerId the customer ID
     * @param searchTerm the search term
     * @return list of matching beneficiaries
     */
    List<BeneficiaryResponse> searchBeneficiaries(String customerId, String searchTerm);

    /**
     * Get most frequently used beneficiaries
     * @param customerId the customer ID
     * @param limit maximum number of results
     * @return list of most used beneficiaries
     */
    List<BeneficiaryResponse> getMostUsedBeneficiaries(String customerId, int limit);

    /**
     * Update transfer statistics after a successful transfer
     * @param customerId the customer ID
     * @param beneficiaryAccountNumber the beneficiary account number
     */
    void updateTransferStats(String customerId, String beneficiaryAccountNumber);
}
