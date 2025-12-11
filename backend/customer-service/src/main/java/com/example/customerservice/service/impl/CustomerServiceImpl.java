package com.example.customerservice.service.impl;

import com.example.commonapi.dto.ApiResponse;
import com.example.commonapi.dto.account.AccountSyncRequest;
import com.example.commonapi.dto.account.AccountSyncResult;
import com.example.customerservice.client.CoreBankingFeignClient;
import com.example.customerservice.dto.request.CreateCoreCustomerRequest;
import com.example.customerservice.dto.request.AddressRequest;
import com.example.customerservice.dto.request.CustomerLoginDTO;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.request.CustomerUpdateRequest;
import com.example.customerservice.dto.request.UpdateCoreKycStatusRequest;
import com.example.customerservice.dto.response.CreateCoreCustomerResponse;
import com.example.customerservice.dto.response.CoreCifResponse;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.dubbo.consumer.AccountSyncConsumer;
import com.example.customerservice.entity.Customer;
import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.entity.enums.RiskLevel;
import com.example.customerservice.exception.CustomerAlreadyExistsException;
import com.example.customerservice.exception.CustomerNotFoundException;
import com.example.customerservice.exception.ErrorCode;
import com.example.customerservice.exception.CustomerRegistrationException;
import com.example.customerservice.mapper.AddressMapper;
import com.example.customerservice.mapper.CustomerMapper;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.AddressNormalizationService;
import com.example.customerservice.service.CustomerService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final KeycloakService keycloakService;
    private final CoreBankingFeignClient coreBankingFeignClient;
    private final AccountSyncConsumer accountSyncConsumer;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;
    private final AddressNormalizationService addressNormalizationService;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegisterRequest registerDto) {

        String authProviderId = null;
        String coreCustomerId = null;
        String accountNumber = null;
        Customer savedCustomer = null;

        try {
            // Step 1: Create user in Keycloak 
            log.info("Step 1: Creating user in Keycloak for phone: {}", registerDto.getPhoneNumber());
            authProviderId = keycloakService.createUser(registerDto);
            log.info("Successfully created Keycloak user with ID: {}", authProviderId);

            // Step 2: Create CIF in Core Banking
            log.info("Step 2: Creating CIF in Core Banking System");
            CreateCoreCustomerRequest coreRequest = CreateCoreCustomerRequest.builder()
                    .customerName(registerDto.getFullName())
                    .username(registerDto.getPhoneNumber())
                    .nationalId(registerDto.getNationalId())
                    .nationality(registerDto.getNationality())
                    .issueDateNationalId(registerDto.getIssueDateNationalId())
                    .placeOfIssueNationalId(registerDto.getPlaceOfIssueNationalId())
                    .occupation(registerDto.getOccupation())
                    .position(registerDto.getPosition())
                    .kycStatus(KycStatus.PENDING)
                    .riskLevel(RiskLevel.LOW)
                    .build();

            ApiResponse<CreateCoreCustomerResponse> coreApiResponse = coreBankingFeignClient.createCif(coreRequest);
            
            if (coreApiResponse == null || !coreApiResponse.isSuccess() || coreApiResponse.getData() == null) {
                throw new CustomerRegistrationException(ErrorCode.REGISTRATION_FAILED, 
                    null, new RuntimeException("Core Banking returned invalid response"));
            }
            
            CreateCoreCustomerResponse coreResponse = coreApiResponse.getData();
            coreCustomerId = coreResponse.getCoreCustomerId();
            accountNumber = coreResponse.getAccountNumber();
            log.info("Successfully created CIF: {}, CASA account: {}", coreResponse.getCifNumber(), accountNumber);

            // Step 3: Save customer in local database
            log.info("Step 3: Saving customer in local database");
                AddressRequest normalizedPermanentAddress = addressNormalizationService.normalize(registerDto.getPermanentAddress());
                AddressRequest normalizedTemporaryAddress = registerDto.getTemporaryAddress() != null
                    ? addressNormalizationService.normalize(registerDto.getTemporaryAddress())
                    : null;

                registerDto.setPermanentAddress(normalizedPermanentAddress);
                registerDto.setTemporaryAddress(normalizedTemporaryAddress);

                Customer customer = customerMapper.toEntity(registerDto);
            customer.setAuthProviderId(authProviderId);
            customer.setCifNumber(coreResponse.getCifNumber());
            customer.setEmailVerified(false);

            savedCustomer = customerRepository.save(customer);
            try {
                keycloakService.updateUserAttribute(authProviderId, "customerId", savedCustomer.getCustomerId());
            } catch (Exception e) {
                log.warn("Failed to update customerId to Keycloak attributes", e);
            }
            log.info("Successfully saved customer in database with ID: {}", savedCustomer.getCustomerId());

            // Step 4: Sync account metadata to AccountService via Dubbo
            if (accountNumber != null && !accountNumber.isBlank()) {
                try {
                    log.info("Step 4: Syncing account metadata to AccountService via Dubbo");
                    AccountSyncRequest syncRequest = AccountSyncRequest.builder()
                            .accountNumber(accountNumber)
                            .customerId(savedCustomer.getCustomerId())
                            .cifNumber(coreResponse.getCifNumber())
                            .accountType("CHECKING")
                            .currency("VND")
                            .status("ACTIVE")
                            .balance(BigDecimal.ZERO)
                            .openedAt(LocalDateTime.now())
                            .build();
                    
                    AccountSyncResult syncResult = accountSyncConsumer.syncAccountMetadata(syncRequest);
                    
                    if (syncResult.isSuccess()) {
                        log.info("Successfully synced account metadata to AccountService: accountId={}", syncResult.getAccountId());
                    } else {
                        log.warn("Account sync returned success=false: {}", syncResult.getMessage());
                    }
                } catch (Exception e) {
                    // Log but don't fail registration if Dubbo sync fails
                    log.error("Failed to sync account metadata to AccountService, but registration completed", e);
                }
            }

            return customerMapper.toResponse(savedCustomer);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Duplicate customer detected during save: {}", e.getMessage());
            
            // Rollback external systems
            rollbackKeycloak(authProviderId);
            rollbackCoreBanking(coreCustomerId);
            
            // Determine which field is duplicate
            String message = e.getMessage().toLowerCase();
            if (message.contains("phone") || message.contains("username")) {
                throw new CustomerAlreadyExistsException("phoneNumber", registerDto.getPhoneNumber());
            } else if (message.contains("email")) {
                throw new CustomerAlreadyExistsException("email", registerDto.getEmail());
            } else if (message.contains("national")) {
                throw new CustomerAlreadyExistsException("nationalId", registerDto.getNationalId());
            }
            throw new CustomerAlreadyExistsException("field", "unknown - " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Error during customer registration, initiating rollback", e);

            // Rollback in reverse order
            // Step 3 rollback: Delete from local database if saved
            if (savedCustomer != null) {
                try {
                    log.info("Rollback Step 3: Deleting customer from local database");
                    customerRepository.delete(savedCustomer);
                } catch (Exception rollbackException) {
                    log.error("Failed to rollback local database", rollbackException);
                }
            }

            // Step 2 rollback: Delete from Core Banking if created
            if (coreCustomerId != null) {
                try {
                    log.info("Rollback Step 2: Deleting customer from Core Banking");
                    coreBankingFeignClient.deleteCif(coreCustomerId);
                } catch (Exception rollbackException) {
                    log.error("Failed to rollback Core Banking customer", rollbackException);
                }
            }

            // Step 1 rollback: Delete from Keycloak if created
            if (authProviderId != null) {
                try {
                    log.info("Rollback Step 1: Deleting user from Keycloak");
                    keycloakService.deleteUser(authProviderId);
                } catch (Exception rollbackException) {
                    log.error("Failed to rollback Keycloak user", rollbackException);
                }
            }

            if (e instanceof FeignException) {
                throw new CustomerRegistrationException(ErrorCode.REGISTRATION_FAILED, 
                    null, new RuntimeException("Core Banking service error: " + e.getMessage(), e));
            }
            throw new CustomerRegistrationException(ErrorCode.REGISTRATION_FAILED, null, e);
        }
    }

    @Override
    public Object loginCustomer(CustomerLoginDTO loginDto) {
        log.info("Customer {} is attempting to login", loginDto.getUsername());
        return keycloakService.getToken(loginDto);
    }

    @Override
    public Object refreshToken(String refreshToken) {
        log.info("Refreshing access token");
        return keycloakService.refreshToken(refreshToken);
    }

    @Override
    public CustomerResponse getMyInfo() {
        // Get user info from JWT token in security context
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authProviderId = principal.getSubject(); // 'sub' claim is Keycloak user ID

        log.debug("Fetching customer info for authProviderId: {}", authProviderId);

        Customer customer = customerRepository.findByAuthProviderId(authProviderId)
                .orElseThrow(() -> new CustomerNotFoundException("authProviderId", authProviderId));

        log.info("Found customer: {}", customer.getFullName());
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByAuthProviderId(String authProviderId) {
        log.debug("Fetching customer info for authProviderId: {}", authProviderId);

        Customer customer = customerRepository.findByAuthProviderId(authProviderId)
                .orElseThrow(() -> new CustomerNotFoundException("authProviderId", authProviderId));

        log.info("Found customer: {}", customer.getFullName());
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(String authProviderId, CustomerUpdateRequest updateRequest) {
        Customer customer = customerRepository.findByAuthProviderId(authProviderId)
                .orElseThrow(() -> new CustomerNotFoundException("authProviderId", authProviderId));

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            customer.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isBlank()) {
            customer.setPhoneNumber(updateRequest.getPhone());
        }
        if (updateRequest.getTemporaryAddress() != null) {
            AddressRequest normalizedTemporary = addressNormalizationService.normalize(updateRequest.getTemporaryAddress());
            customer.setTemporaryAddress(addressMapper.toEntity(normalizedTemporary));
        }

        Customer saved = customerRepository.save(customer);
        log.info("Updated customer: {}", saved.getCustomerId());
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void updateKycStatus(String nationalId, KycStatus kycStatus) {
        String normalizedNationalId = nationalId != null ? nationalId.trim() : null;
        log.info("Updating KYC status for nationalId {} to {}", normalizedNationalId, kycStatus);

        if (kycStatus == null) {
            log.warn("Received null KYC status for nationalId {}. Skipping update.", normalizedNationalId);
            return;
        }

        if (normalizedNationalId == null || normalizedNationalId.isEmpty()) {
            log.warn("Cannot update KYC status because nationalId is blank");
            return;
        }

        Customer customer = customerRepository.findByNationalId(normalizedNationalId)
                .orElseThrow(() -> new CustomerNotFoundException("nationalId", normalizedNationalId));

        if (customer.getKycStatus() != kycStatus) {
            customer.setKycStatus(kycStatus);
            customerRepository.save(customer);
            log.info("Updated local KYC status for customer {} to {}", customer.getCustomerId(), kycStatus);
        } else {
            log.debug("KYC status for customer {} is already {}. Skipping local update.", customer.getCustomerId(), kycStatus);
        }

        if (customer.getCifNumber() == null || customer.getCifNumber().isBlank()) {
            log.warn("Customer {} does not have CIF number; skipping core banking KYC update", customer.getCustomerId());
            return;
        }

        UpdateCoreKycStatusRequest request = UpdateCoreKycStatusRequest.builder()
                .kycStatus(kycStatus)
                .authorizedBy("CUSTOMER_SERVICE")
                .note("Updated via customer-service eKYC workflow")
                .build();

        try {
            ApiResponse<CoreCifResponse> response = coreBankingFeignClient.updateKycStatus(customer.getCifNumber(), request);

            if (response == null || !response.isSuccess()) {
                throw new CustomerRegistrationException(ErrorCode.CORE_BANKING_INVALID_RESPONSE, null,
                        new IllegalStateException("Core banking returned unsuccessful response when updating KYC status"));
            }

            log.info("Synchronized KYC status with core banking for CIF {} -> {}", customer.getCifNumber(), kycStatus);
        } catch (FeignException feignException) {
            log.error("Failed to update KYC status in core banking for CIF {}", customer.getCifNumber(), feignException);
            throw new CustomerRegistrationException(ErrorCode.CORE_BANKING_CONNECTION_ERROR, null, feignException);
        } catch (CustomerRegistrationException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Unexpected error while syncing KYC status to core banking for CIF {}", customer.getCifNumber(), ex);
            throw new CustomerRegistrationException(ErrorCode.CORE_BANKING_INVALID_RESPONSE, null, ex);
        }
    }

    @Override
    public CustomerResponse getCustomerById(String customerId) {
        log.debug("Fetching customer by ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("customerId", customerId));
        
        log.info("Found customer: {}", customer.getFullName());
        return customerMapper.toResponse(customer);
    }


    private void rollbackKeycloak(String authProviderId) {
        if (authProviderId != null) {
            try {
                log.info("Rollback: Deleting user from Keycloak");
                keycloakService.deleteUser(authProviderId);
            } catch (Exception rollbackException) {
                log.error("Failed to rollback Keycloak user", rollbackException);
            }
        }
    }

    private void rollbackCoreBanking(String coreCustomerId) {
        if (coreCustomerId != null) {
            try {
                log.info("Rollback: Deleting customer from Core Banking");
                coreBankingFeignClient.deleteCif(coreCustomerId);
            } catch (Exception rollbackException) {
                log.error("Failed to rollback Core Banking customer", rollbackException);
            }
        }
    }
}