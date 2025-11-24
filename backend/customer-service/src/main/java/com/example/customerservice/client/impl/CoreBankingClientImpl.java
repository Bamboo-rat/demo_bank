package com.example.customerservice.client.impl;

import com.example.customerservice.client.CoreBankingClient;
import com.example.customerservice.dto.request.CreateCoreCustomerRequest;
import com.example.customerservice.dto.response.CreateCoreCustomerResponse;
import com.example.customerservice.exception.CoreBankingException;
import com.example.customerservice.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreBankingClientImpl implements CoreBankingClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CreateCoreCustomerResponse createCoreCustomer(CreateCoreCustomerRequest request) {
        try {
            log.info("Creating core customer for username: {}", request.getUsername());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateCoreCustomerRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    "/api/cif/create",
                    HttpMethod.POST,
                    entity,
                    Object.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse the API response wrapper
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                CreateCoreCustomerResponse coreResponse = objectMapper.convertValue(data, CreateCoreCustomerResponse.class);
                log.info("Successfully created core customer with ID: {}", coreResponse.getCoreCustomerId());
                return coreResponse;
            } else {
                throw new CoreBankingException(ErrorCode.CORE_BANKING_INVALID_RESPONSE,
                        Map.of("username", request.getUsername()));
            }

        } catch (HttpClientErrorException e) {
            log.error("Client error creating core customer for username: {}", request.getUsername(), e);
            throw new CoreBankingException(ErrorCode.CORE_BANKING_CREATE_CUSTOMER_FAILED,
                    Map.of("username", request.getUsername(), "status", e.getStatusCode().value(), "response", e.getResponseBodyAsString()), e);
        } catch (HttpServerErrorException e) {
            log.error("Server error creating core customer for username: {}", request.getUsername(), e);
            throw new CoreBankingException(ErrorCode.CORE_BANKING_SERVICE_UNAVAILABLE,
                    Map.of("username", request.getUsername(), "status", e.getStatusCode().value()), e);
        } catch (ResourceAccessException e) {
            log.error("Connection error creating core customer for username: {}", request.getUsername(), e);
            throw new CoreBankingException(ErrorCode.CORE_BANKING_CONNECTION_ERROR,
                    Map.of("username", request.getUsername()), e);
        } catch (Exception e) {
            log.error("Unexpected error creating core customer for username: {}", request.getUsername(), e);
            throw new CoreBankingException(ErrorCode.CORE_BANKING_CREATE_CUSTOMER_FAILED,
                    Map.of("username", request.getUsername()), e);
        }
    }

    @Override
    public void deleteCoreCustomer(String coreCustomerId) {
        try {
            log.info("Deleting core customer with ID: {}", coreCustomerId);

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/cif/{cifId}",
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    coreCustomerId
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully deleted core customer with ID: {}", coreCustomerId);
            } else {
                log.warn("Failed to delete core customer with ID: {}", coreCustomerId);
            }

        } catch (Exception e) {
            log.error("Error deleting core customer with ID: {}", coreCustomerId, e);
            // Don't throw exception for delete operation in rollback scenario
        }
    }
}