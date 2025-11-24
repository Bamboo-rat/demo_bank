package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.request.CreateCifRequest;
import com.example.corebankingservice.dto.request.UpdateCifStatusRequest;
import com.example.corebankingservice.dto.response.CifResponse;
import com.example.corebankingservice.dto.response.CifStatusResponse;
import com.example.corebankingservice.entity.CIF_Master;
import com.example.corebankingservice.entity.enums.CustomerStatus;
import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.repository.CifMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CifService {

    private final CifMasterRepository cifMasterRepository;
    private static final String CIF_PREFIX = "CIF";

    /**
     * Create new CIF
     */
    public CifResponse createCif(CreateCifRequest request) {
        log.info("Creating new CIF for username: {}", request.getUsername());

        // Validate unique constraints
        if (cifMasterRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }

        if (cifMasterRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessException("National ID already registered: " + request.getNationalId());
        }

        // Generate CIF Number
        String cifNumber = generateCifNumber();

        // Create CIF entity
        CIF_Master cif = CIF_Master.builder()
                .cifNumber(cifNumber)
                .customerName(request.getCustomerName())
                .username(request.getUsername())
                .nationalId(request.getNationalId())
                .customerStatus(CustomerStatus.ACTIVE)
                .kycStatus(request.getKycStatus())
                .riskLevel(request.getRiskLevel())
                .build();

        CIF_Master savedCif = cifMasterRepository.save(cif);
        log.info("CIF created successfully with number: {}", cifNumber);

        return mapToResponse(savedCif);
    }

    /**
     * Get CIF Status
     */
    @Transactional(readOnly = true)
    public CifStatusResponse getCifStatus(String cifNumber) {
        log.info("Getting CIF status for: {}", cifNumber);

        CIF_Master cif = cifMasterRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new ResourceNotFoundException("CIF not found: " + cifNumber));

        return CifStatusResponse.builder()
                .cifNumber(cif.getCifNumber())
                .customerName(cif.getCustomerName())
                .status(cif.getCustomerStatus())
                .kycStatus(cif.getKycStatus())
                .riskLevel(cif.getRiskLevel())
                .canTransact(canTransact(cif))
                .build();
    }

    /**
     * Update CIF Status
     */
    public CifResponse updateCifStatus(String cifNumber, UpdateCifStatusRequest request) {
        log.info("Updating CIF status for: {} to {}", cifNumber, request.getAction());

        CIF_Master cif = cifMasterRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new ResourceNotFoundException("CIF not found: " + cifNumber));

        CustomerStatus newStatus = mapActionToStatus(request.getAction());
        CustomerStatus oldStatus = cif.getCustomerStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);

        cif.setCustomerStatus(newStatus);

        if (newStatus == CustomerStatus.CLOSED) {
            cif.setLastTransactionDate(LocalDateTime.now());
        }

        CIF_Master updatedCif = cifMasterRepository.save(cif);

        log.info("CIF status updated from {} to {} for: {}", oldStatus, newStatus, cifNumber);

        return mapToResponse(updatedCif);
    }

    /**
     * Check if customer can transact
     */
    private boolean canTransact(CIF_Master cif) {
        return cif.getCustomerStatus() == CustomerStatus.ACTIVE &&
                cif.getKycStatus() == KycStatus.VERIFIED &&
                cif.getRiskLevel() != RiskLevel.HIGH;
    }

    /**
     * Generate unique CIF number
     */
    private String generateCifNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", new Random().nextInt(999999));
        return CIF_PREFIX + timestamp + random;
    }

    /**
     * Map action to status
     */
    private CustomerStatus mapActionToStatus(String action) {
        return switch (action.toUpperCase()) {
            case "ACTIVATE" -> CustomerStatus.ACTIVE;
            case "SUSPEND" -> CustomerStatus.SUSPENDED;
            case "BLOCK" -> CustomerStatus.BLOCKED;
            case "CLOSE" -> CustomerStatus.CLOSED;
            default -> throw new BusinessException("Invalid action: " + action);
        };
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(CustomerStatus current, CustomerStatus target) {
        // Cannot reactivate closed account
        if (current == CustomerStatus.CLOSED && target != CustomerStatus.CLOSED) {
            throw new BusinessException("Cannot reactivate closed account");
        }

        // Cannot directly close blocked account
        if (current == CustomerStatus.BLOCKED && target == CustomerStatus.CLOSED) {
            throw new BusinessException("Must unblock account before closing");
        }
    }

    /**
     * Map entity to response
     */
    private CifResponse mapToResponse(CIF_Master cif) {
        return CifResponse.builder()
                .cifId(cif.getCifId())
                .cifNumber(cif.getCifNumber())
                .customerName(cif.getCustomerName())
                .username(cif.getUsername())
                .customerStatus(cif.getCustomerStatus())
                .kycStatus(cif.getKycStatus())
                .riskLevel(cif.getRiskLevel())
                .createdDate(cif.getCreatedDate())
                .build();
    }
}