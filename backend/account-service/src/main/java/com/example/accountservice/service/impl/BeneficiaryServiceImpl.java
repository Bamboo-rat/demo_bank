package com.example.accountservice.service.impl;

import com.example.accountservice.dto.request.CreateBeneficiaryRequest;
import com.example.accountservice.dto.request.UpdateBeneficiaryRequest;
import com.example.accountservice.dto.response.BeneficiaryResponse;
import com.example.accountservice.entity.Beneficiary;
import com.example.accountservice.exception.BeneficiaryAlreadyExistsException;
import com.example.accountservice.exception.BeneficiaryNotFoundException;
import com.example.accountservice.exception.UnauthorizedBeneficiaryAccessException;
import com.example.accountservice.mapper.BeneficiaryMapper;
import com.example.accountservice.repository.BeneficiaryRepository;
import com.example.accountservice.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryMapper beneficiaryMapper;

    @Override
    public BeneficiaryResponse createBeneficiary(String customerId, CreateBeneficiaryRequest request) {
        // Check if beneficiary already exists
        if (beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountNumber(
                customerId, request.getBeneficiaryAccountNumber())) {
            throw new BeneficiaryAlreadyExistsException(
                    "Beneficiary account already exists in your list");
        }

        Beneficiary beneficiary = beneficiaryMapper.toEntity(request);
        beneficiary.setCustomerId(customerId);

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Created beneficiary {} for customer {}", saved.getBeneficiaryId(), customerId);

        return beneficiaryMapper.toResponse(saved);
    }

    @Override
    public BeneficiaryResponse updateBeneficiary(String customerId, String beneficiaryId, UpdateBeneficiaryRequest request) {
        Beneficiary beneficiary = getBeneficiaryEntity(customerId, beneficiaryId);

        beneficiaryMapper.updateEntity(request, beneficiary);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Updated beneficiary {} for customer {}", beneficiaryId, customerId);

        return beneficiaryMapper.toResponse(updated);
    }

    @Override
    public void deleteBeneficiary(String customerId, String beneficiaryId) {
        Beneficiary beneficiary = getBeneficiaryEntity(customerId, beneficiaryId);

        beneficiaryRepository.delete(beneficiary);
        log.info("Deleted beneficiary {} for customer {}", beneficiaryId, customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiary(String customerId, String beneficiaryId) {
        Beneficiary beneficiary = getBeneficiaryEntity(customerId, beneficiaryId);
        return beneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getAllBeneficiaries(String customerId) {
        List<Beneficiary> beneficiaries = beneficiaryRepository
                .findByCustomerIdOrderByLastTransferDateDesc(customerId);
        return beneficiaryMapper.toResponseList(beneficiaries);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BeneficiaryResponse> getBeneficiaries(String customerId, Pageable pageable) {
        Page<Beneficiary> beneficiaries = beneficiaryRepository.findByCustomerId(customerId, pageable);
        return beneficiaries.map(beneficiaryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> searchBeneficiaries(String customerId, String searchTerm) {
        List<Beneficiary> beneficiaries = beneficiaryRepository
                .searchByNameOrNickname(customerId, searchTerm);
        return beneficiaryMapper.toResponseList(beneficiaries);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getMostUsedBeneficiaries(String customerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Beneficiary> beneficiaries = beneficiaryRepository
                .findMostUsedBeneficiaries(customerId, pageable);
        return beneficiaryMapper.toResponseList(beneficiaries);
    }

    @Override
    public void updateTransferStats(String customerId, String beneficiaryAccountNumber) {
        beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(customerId, beneficiaryAccountNumber)
                .ifPresent(beneficiary -> {
                    beneficiaryRepository.updateTransferStats(
                            beneficiary.getBeneficiaryId(),
                            LocalDateTime.now()
                    );
                    log.debug("Updated transfer stats for beneficiary {}", beneficiary.getBeneficiaryId());
                });
    }

    private Beneficiary getBeneficiaryEntity(String customerId, String beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new BeneficiaryNotFoundException(
                        "Beneficiary not found: " + beneficiaryId));

        // Verify ownership
        if (!beneficiary.getCustomerId().equals(customerId)) {
            throw new UnauthorizedBeneficiaryAccessException(
                    "You are not authorized to access this beneficiary");
        }

        return beneficiary;
    }
}
