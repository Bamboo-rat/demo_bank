package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.request.CreateCifRequest;
import com.example.corebankingservice.dto.request.OpenAccountCoreRequest;
import com.example.corebankingservice.dto.request.UpdateCifStatusRequest;
import com.example.corebankingservice.dto.response.AccountDetailResponse;
import com.example.corebankingservice.dto.response.CifResponse;
import com.example.corebankingservice.dto.response.CifStatusResponse;
import com.example.corebankingservice.entity.CIF_Master;
import com.example.corebankingservice.entity.enums.AccountType;
import com.example.corebankingservice.entity.enums.Currency;
import com.example.corebankingservice.entity.enums.CustomerStatus;
import com.example.corebankingservice.entity.enums.KycStatus;
import com.example.corebankingservice.entity.enums.RiskLevel;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.mapper.CifMapper;
import com.example.corebankingservice.repository.CifMasterRepository;
import com.example.corebankingservice.service.AccountLifecycleService;
import com.example.corebankingservice.service.CifService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CifServiceImpl implements CifService{

    private final CifMasterRepository cifMasterRepository;
    private final CifMapper cifMapper;
    private final AccountLifecycleService accountLifecycleService;
    private final MessageSource messageSource;
    private static final String CIF_PREFIX = "CIF";

    /**
     * Create new CIF
     */
    @Override
    public CifResponse createCif(CreateCifRequest request) {
        log.info("Creating new CIF for username: {}", request.getUsername());

        // Validate unique constraints
        if (cifMasterRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(getMessage("error.customer.username.exists"));
        }

        if (cifMasterRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessException(getMessage("error.customer.nationalid.exists"));
        }

        String cifNumber = generateCifNumber();

        CIF_Master cif = cifMapper.toNewEntity(request, cifNumber);

        CIF_Master savedCif = cifMasterRepository.save(cif);
        log.info("CIF created successfully with number: {}", cifNumber);

        try {
            log.info("Auto opening CASA account for CIF: {}", cifNumber);
            OpenAccountCoreRequest casaRequest = OpenAccountCoreRequest.builder()
                    .cifNumber(cifNumber)
                    .accountType(AccountType.CHECKING)
                    .currency(Currency.VND)
                    .createdBy("SYSTEM")
                    .description("Auto-created CASA account during customer registration")
                    .build();
            
            AccountDetailResponse casaAccount = accountLifecycleService.openAccount(casaRequest);
            log.info("CASA account {} opened successfully for CIF {}", casaAccount.getAccountNumber(), cifNumber);
            
            return cifMapper.toResponseWithAccount(savedCif, casaAccount.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to auto-create CASA account for CIF: {}", cifNumber, e);
            // Note: CIF is already created, this is logged but not rolled back
            // Consider implementing saga pattern or compensation logic if needed
            return cifMapper.toResponse(savedCif);
        }
    }

    /**
     * Get CIF Status
     */
    @Override
    @Transactional(readOnly = true)
    public CifStatusResponse getCifStatus(String cifNumber) {
        log.info("Getting CIF status for: {}", cifNumber);

        CIF_Master cif = cifMasterRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.cif.not.found")));

        return cifMapper.toStatusResponse(cif, canTransact(cif));
    }

    /**
     * Update CIF Status
     */
    @Override
    public CifResponse updateCifStatus(String cifNumber, UpdateCifStatusRequest request) {
        log.info("Updating CIF status for: {} to {}", cifNumber, request.getAction());

        CIF_Master cif = cifMasterRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.cif.not.found")));

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

        return cifMapper.toResponse(updatedCif);
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
            throw new BusinessException(getMessage("error.customer.cannot.reactivate.closed"));
        }

        // Cannot directly close blocked account
        if (current == CustomerStatus.BLOCKED && target == CustomerStatus.CLOSED) {
            throw new BusinessException(getMessage("error.customer.cannot.close.blocked"));
        }
    }

    private String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }

}