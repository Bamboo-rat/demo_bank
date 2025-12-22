package com.example.accountservice.service.impl;

import com.example.accountservice.service.SavingsInterestRateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Implementation của SavingsInterestRateService
 * Load lãi suất từ file JSON và cung cấp API query
 */
@Service
@Slf4j
public class SavingsInterestRateServiceImpl implements SavingsInterestRateService {

    private JsonNode interestRates;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadInterestRates() {
        try {
            ClassPathResource resource = new ClassPathResource("data/saving.json");
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            this.interestRates = root.get("interest_rates");
            log.info("Loaded savings interest rates configuration successfully");
        } catch (IOException e) {
            log.error("Failed to load savings interest rates from JSON file", e);
            throw new RuntimeException("Failed to load savings interest rates", e);
        }
    }

    @Override
    public BigDecimal getInterestRate(int termMonths, String paymentMethod) {
        if (interestRates == null) {
            log.error("Interest rates not loaded");
            throw new IllegalStateException("Interest rates configuration not loaded");
        }

        try {
            JsonNode termDeposits = interestRates.get("term_deposit_vnd");
            
            // Nếu không có kỳ hạn (NO_TERM), dùng lãi suất tiết kiệm không kỳ hạn
            if (termMonths == 0) {
                JsonNode nonTerm = interestRates.get("non_term_vnd");
                if (nonTerm != null && nonTerm.isArray() && nonTerm.size() > 0) {
                    double rate = nonTerm.get(0).get("rate").asDouble();
                    return BigDecimal.valueOf(rate);
                }
                return BigDecimal.valueOf(0.50); // Default fallback
            }

            // Tìm term phù hợp
            for (JsonNode term : termDeposits) {
                String termStr = term.get("term").asText();
                int months = parseTermMonths(termStr);
                
                if (months == termMonths) {
                    return getRateByPaymentMethod(term, paymentMethod);
                }
            }

            log.warn("Interest rate not found for termMonths={}, paymentMethod={}", termMonths, paymentMethod);
            return getDefaultRate(termMonths);

        } catch (Exception e) {
            log.error("Error getting interest rate for termMonths={}, paymentMethod={}", termMonths, paymentMethod, e);
            return getDefaultRate(termMonths);
        }
    }

    /**
     * Lấy lãi suất theo phương thức trả lãi
     */
    private BigDecimal getRateByPaymentMethod(JsonNode term, String paymentMethod) {
        switch (paymentMethod) {
            case "END_OF_TERM":
                return BigDecimal.valueOf(term.get("end_term_rate").asDouble());
                
            case "BEGINNING":
                JsonNode prepaidRate = term.get("prepaid_rate");
                if (prepaidRate != null && !prepaidRate.isNull()) {
                    return BigDecimal.valueOf(prepaidRate.asDouble());
                }
                // Fallback to end_term_rate
                return BigDecimal.valueOf(term.get("end_term_rate").asDouble());
                
            case "MONTHLY":
                return getPeriodicRate(term, "1_month");
                
            case "QUARTERLY":
                return getPeriodicRate(term, "3_months");
                
            default:
                return BigDecimal.valueOf(term.get("end_term_rate").asDouble());
        }
    }

    /**
     * Lấy lãi suất trả định kỳ
     */
    private BigDecimal getPeriodicRate(JsonNode term, String period) {
        JsonNode periodicRates = term.get("periodic_rates");
        if (periodicRates != null && periodicRates.has(period)) {
            JsonNode rate = periodicRates.get(period);
            if (rate != null && !rate.isNull()) {
                return BigDecimal.valueOf(rate.asDouble());
            }
        }
        // Fallback to end_term_rate
        return BigDecimal.valueOf(term.get("end_term_rate").asDouble());
    }

    /**
     * Parse term string thành số tháng
     */
    private int parseTermMonths(String termStr) {
        termStr = termStr.toLowerCase().trim();
        
        if (termStr.contains("tuần")) {
            return 0; // Kỳ hạn tuần không hỗ trợ
        }
        
        // Extract số
        String[] parts = termStr.split("\\s+");
        if (parts.length > 0) {
            try {
                return Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse term months from: {}", termStr);
            }
        }
        
        return 0;
    }

    /**
     * Lãi suất mặc định theo kỳ hạn
     */
    private BigDecimal getDefaultRate(int termMonths) {
        if (termMonths == 0) return BigDecimal.valueOf(0.50);
        if (termMonths <= 3) return BigDecimal.valueOf(3.50);
        if (termMonths <= 6) return BigDecimal.valueOf(5.30);
        if (termMonths <= 12) return BigDecimal.valueOf(5.50);
        return BigDecimal.valueOf(5.25);
    }
}
