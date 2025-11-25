package com.example.customerservice.service.impl;

import com.example.customerservice.dto.request.CreateCoreCustomerRequest;
import com.example.customerservice.dto.request.CustomerLoginDTO;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.request.CustomerUpdateRequest;
import com.example.customerservice.dto.response.CreateCoreCustomerResponse;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.entity.Customer;
import com.example.customerservice.entity.enums.CustomerStatus;
import com.example.customerservice.entity.enums.KycStatus;
import com.example.customerservice.entity.enums.RiskLevel;
import com.example.customerservice.exception.CoreBankingException;
import com.example.customerservice.exception.CustomerAlreadyExistsException;
import com.example.customerservice.exception.CustomerNotFoundException;
import com.example.customerservice.exception.ErrorCode;
import com.example.customerservice.exception.CustomerRegistrationException;
import com.example.customerservice.mapper.AddressMapper;
import com.example.customerservice.mapper.CustomerMapper;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.client.CoreBankingClient;
import com.example.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final KeycloakService keycloakService;
    private final CoreBankingClient coreBankingClient;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegisterRequest registerDto) {

        String authProviderId = null;
        String coreCustomerId = null;
        Customer savedCustomer = null;

        try {
            // Step 1: Create user in Keycloak 
            log.info("Step 1: Creating user in Keycloak for phone: {}", registerDto.getPhoneNumber());
            authProviderId = keycloakService.createUser(registerDto);
            log.info("Successfully created Keycloak user with ID: {}", authProviderId);

            // Step 2: Create customer in Core Banking System (CIF)
            log.info("Step 2: Creating customer in Core Banking System");
            CreateCoreCustomerRequest coreRequest = CreateCoreCustomerRequest.builder()
                    .customerName(registerDto.getFullName())
                    .username(registerDto.getPhoneNumber())
                    .nationalId(registerDto.getNationalId())
                    .kycStatus(KycStatus.PENDING)
                    .riskLevel(RiskLevel.LOW)
                    .build();

            CreateCoreCustomerResponse coreResponse = coreBankingClient.createCoreCustomer(coreRequest);
            coreCustomerId = coreResponse.getCoreCustomerId();
            log.info("Successfully created core customer with ID: {}", coreCustomerId);

            // Step 3: Save customer in local database
            log.info("Step 3: Saving customer in local database");
            Customer customer = customerMapper.toEntity(registerDto);
            customer.setAuthProviderId(authProviderId);
            customer.setCoreBankingId(coreCustomerId);
            customer.setStatus(CustomerStatus.PENDING_APPROVAL);
            customer.setKycStatus(KycStatus.PENDING);
            customer.setRiskLevel(RiskLevel.LOW);
            customer.setEmailVerified(false);

            savedCustomer = customerRepository.save(customer);
            try {
                keycloakService.updateUserAttribute(authProviderId, "customerId", savedCustomer.getCustomerId());
            } catch (Exception e) {
                log.warn("Failed to update customerId to Keycloak attributes", e);
            }
            log.info("Successfully saved customer in database with ID: {}", savedCustomer.getCustomerId());

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
                    coreBankingClient.deleteCoreCustomer(coreCustomerId);
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

            if (e instanceof CoreBankingException) {
                throw e;
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

        // Update only provided fields
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            customer.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isBlank()) {
            customer.setPhoneNumber(updateRequest.getPhone());
        }
        if (updateRequest.getTemporaryAddress() != null) {
            customer.setTemporaryAddress(addressMapper.toEntity(updateRequest.getTemporaryAddress()));
        }

        Customer saved = customerRepository.save(customer);
        log.info("Updated customer: {}", saved.getCustomerId());
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void updateKycStatus(String nationalId, KycStatus kycStatus) {
        Customer customer = customerRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new CustomerNotFoundException("nationalId", nationalId));

        customer.setKycStatus(kycStatus);
        customerRepository.save(customer);

        log.info("Updated KYC status for customer {} to {}", customer.getFullName(), kycStatus);
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
                coreBankingClient.deleteCoreCustomer(coreCustomerId);
            } catch (Exception rollbackException) {
                log.error("Failed to rollback Core Banking customer", rollbackException);
            }
        }
    }
}