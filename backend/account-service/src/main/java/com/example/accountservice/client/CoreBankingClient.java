package com.example.accountservice.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import com.example.accountservice.dto.request.AccountLifecycleActionRequest;
import com.example.accountservice.dto.request.OpenAccountCoreRequest;
import com.example.accountservice.dto.response.AccountDetailResponse;
import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import com.example.accountservice.entity.enums.Currency;
import com.example.commonapi.dto.ApiResponse;
import org.springframework.core.ParameterizedTypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreBankingClient {

    private final RestTemplate restTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    @Value("${core-banking.base-url:http://localhost:8088}")
    private String coreBankingBaseUrl;

    public AccountDetailResponse openAccount(OpenAccountCoreRequest request) {
        return executeWithResilience("core-open-account", () -> doOpenAccount(request));
    }

    public void processDeposit(String accountNumber, BigDecimal amount, String description) {
        executeWithResilience("core-deposit", () -> doDeposit(accountNumber, amount, description));
    }

    public void closeAccount(String accountNumber, AccountLifecycleActionRequest request) {
        executeWithResilience("core-close-account", () -> doClose(accountNumber, request));
    }

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

    private AccountDetailResponse doOpenAccount(OpenAccountCoreRequest request) {
        log.info("Calling core to open account for CIF {}", request.getCifNumber());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OpenAccountCoreRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ApiResponse<Map<String,Object>>> response = restTemplate.exchange(
                coreBankingBaseUrl + "/api/accounts",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Map<String,Object>>>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Core openAccount failed: " + response.getStatusCode());
        }

        Map<String, Object> data = response.getBody().getData();
        AccountDetailResponse detail = AccountDetailResponse.builder()
                .accountId((String) data.get("accountId"))
                .accountNumber((String) data.get("accountNumber"))
                .cifNumber((String) data.get("cifNumber"))
                .accountType(AccountType.valueOf((String) data.get("accountType")))
                .status(AccountStatus.valueOf((String) data.get("status")))
                .currency(Currency.valueOf((String) data.get("currency")))
                .build();

        log.info("Core created account {} for CIF {}", detail.getAccountNumber(), detail.getCifNumber());
        return detail;
    }

    private Void doDeposit(String accountNumber, BigDecimal amount, String description) {
        log.info("Processing deposit of {} to account {}", amount, accountNumber);

        Map<String, Object> request = new HashMap<>();
        request.put("accountNumber", accountNumber);
        request.put("amount", amount);
        request.put("transactionType", "DEPOSIT");
        request.put("description", description);
        request.put("channel", "SYSTEM");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map<String,Object>> response = restTemplate.exchange(
                coreBankingBaseUrl + "/api/transactions/deposit",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String,Object>>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Deposit failed: " + response.getStatusCode());
        }

        log.info("Deposit processed successfully for account {}", accountNumber);
        return null;
    }

    private Void doClose(String accountNumber, AccountLifecycleActionRequest request) {
        log.info("Calling core to close account {}", accountNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AccountLifecycleActionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                coreBankingBaseUrl + "/api/accounts/" + accountNumber + "/close",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Object>>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Core closeAccount failed: " + response.getStatusCode());
        }

        log.info("Core closed account {} successfully", accountNumber);
        return null;
    }
}
