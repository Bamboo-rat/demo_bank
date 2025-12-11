package com.example.accountservice.controller;

import com.example.accountservice.dto.request.CreateBeneficiaryRequest;
import com.example.accountservice.dto.request.UpdateBeneficiaryRequest;
import com.example.accountservice.dto.response.BeneficiaryResponse;
import com.example.accountservice.service.BeneficiaryService;
import com.example.commonapi.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing beneficiaries (danh bạ thụ hưởng)
 */
@RestController
@RequestMapping("/api/customers/{customerId}/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    /**
     * Create a new beneficiary
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> createBeneficiary(
            @PathVariable String customerId,
            @RequestBody @Valid CreateBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.createBeneficiary(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary created successfully", response));
    }

    /**
     * Update beneficiary details
     */
    @PutMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> updateBeneficiary(
            @PathVariable String customerId,
            @PathVariable String beneficiaryId,
            @RequestBody @Valid UpdateBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(customerId, beneficiaryId, request);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully", response));
    }

    /**
     * Delete a beneficiary
     */
    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @PathVariable String customerId,
            @PathVariable String beneficiaryId) {
        beneficiaryService.deleteBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }

    /**
     * Get beneficiary by ID
     */
    @GetMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> getBeneficiary(
            @PathVariable String customerId,
            @PathVariable String beneficiaryId) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary fetched successfully", response));
    }

    /**
     * Get all beneficiaries (no pagination)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getAllBeneficiaries(
            @PathVariable String customerId) {
        List<BeneficiaryResponse> response = beneficiaryService.getAllBeneficiaries(customerId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries fetched successfully", response));
    }

    /**
     * Get beneficiaries with pagination
     */
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<BeneficiaryResponse>>> getBeneficiariesPaged(
            @PathVariable String customerId,
            @PageableDefault(size = 20, sort = "lastTransferDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BeneficiaryResponse> response = beneficiaryService.getBeneficiaries(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries fetched successfully", response));
    }

    /**
     * Search beneficiaries by name or nickname
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> searchBeneficiaries(
            @PathVariable String customerId,
            @RequestParam String q) {
        List<BeneficiaryResponse> response = beneficiaryService.searchBeneficiaries(customerId, q);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response));
    }

    /**
     * Get most frequently used beneficiaries
     */
    @GetMapping("/most-used")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getMostUsedBeneficiaries(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "5") int limit) {
        List<BeneficiaryResponse> response = beneficiaryService.getMostUsedBeneficiaries(customerId, limit);
        return ResponseEntity.ok(ApiResponse.success("Most used beneficiaries fetched successfully", response));
    }
}
