package com.example.customerservice.client.impl;

import com.example.customerservice.client.CoreBankingClient;
import com.example.customerservice.dto.request.CreateCoreCustomerRequest;
import com.example.customerservice.dto.response.CreateCoreCustomerResponse;
import com.example.customerservice.exception.CoreBankingException;
import com.example.customerservice.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreBankingClientImpl implements CoreBankingClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    @Override
    public CreateCoreCustomerResponse createCoreCustomer(CreateCoreCustomerRequest request) {
        return executeWithResilience("core-create-customer", () -> doCreateCoreCustomer(request));
    }

    @Override
    public void deleteCoreCustomer(String coreCustomerId) {
        try {
            executeWithResilience("core-delete-customer", () -> doDeleteCoreCustomer(coreCustomerId));
        } catch (Exception e) {
            log.error("Error deleting core customer with ID: {}", coreCustomerId, e);
            // Don't throw exception for delete operation in rollback scenario
        }
    }

    // ===== Internal execution with Resilience4j (CB + Retry + TimeLimiter) =====
    private <T> T executeWithResilience(String name, Supplier<T> supplier) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
        Retry retry = retryRegistry.retry(name);
        TimeLimiter tl = timeLimiterRegistry.timeLimiter(name);

        Supplier<T> withCb = CircuitBreaker.decorateSupplier(cb, supplier);
        Supplier<T> withRetry = Retry.decorateSupplier(retry, withCb);

        try {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(withRetry);
            return tl.executeFutureSupplier(() -> future);
        } catch (TimeoutException te) {
            log.error("Operation {} timed out", name, te);
            throw new RuntimeException("Core operation timed out: " + name, te);
        } catch (Exception e) {
            log.error("Operation {} failed", name, e);
            throw (e instanceof RuntimeException re) ? re : new RuntimeException("Core operation failed: " + name, e);
        }
    }

    // ===== Raw REST calls (decorated by executeWithResilience) =====
    private CreateCoreCustomerResponse doCreateCoreCustomer(CreateCoreCustomerRequest request) {
        try {
            log.info("Calling core to create customer for username: {}", request.getUsername());

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

    private Void doDeleteCoreCustomer(String coreCustomerId) {
        log.info("Calling core to delete customer with ID: {}", coreCustomerId);

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
        return null;
    }
}