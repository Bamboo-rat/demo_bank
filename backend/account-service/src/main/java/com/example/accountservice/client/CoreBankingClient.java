package com.example.accountservice.client;

import com.example.accountservice.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreBankingClient {

    private final RestTemplate restTemplate;

    @Value("${core-banking.base-url:http://localhost:8081}")
    private String coreBankingBaseUrl;

    public void registerAccount(Account account) {
        try {
            log.info("Registering account {} with Core Banking", account.getAccountNumber());

            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", account.getAccountNumber());
            request.put("customerId", account.getCustomerId());
            request.put("accountType", account.getAccountType().toString());
            request.put("currency", account.getCurrency().toString());
            request.put("status", account.getStatus().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    coreBankingBaseUrl + "/api/accounts/register",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Failed to register account: " + response.getStatusCode());
            }

            log.info("Account {} successfully registered with Core Banking", account.getAccountNumber());

        } catch (Exception e) {
            log.error("Error registering account with Core Banking", e);
            throw new RuntimeException("Core Banking registration failed: " + e.getMessage());
        }
    }

    public void processDeposit(String accountNumber, BigDecimal amount, String description) {
        try {
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

            ResponseEntity<Map> response = restTemplate.exchange(
                    coreBankingBaseUrl + "/api/transactions/deposit",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Deposit failed: " + response.getStatusCode());
            }

            log.info("Deposit processed successfully for account {}", accountNumber);

        } catch (Exception e) {
            log.error("Error processing deposit", e);
            throw new RuntimeException("Deposit processing failed: " + e.getMessage());
        }
    }

    public void notifyAccountClosure(String accountNumber) {
        try {
            log.info("Notifying Core Banking about account closure: {}", accountNumber);

            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("action", "CLOSE");
            request.put("reason", "Customer requested closure");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    coreBankingBaseUrl + "/api/accounts/" + accountNumber + "/close",
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );

            log.info("Core Banking notified about account closure: {}", accountNumber);

        } catch (Exception e) {
            log.error("Error notifying Core Banking about account closure", e);
            // This is not critical, so we just log the error
        }
    }
}
