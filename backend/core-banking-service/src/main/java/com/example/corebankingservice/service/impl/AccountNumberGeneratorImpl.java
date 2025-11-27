package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.entity.enums.AccountType;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.service.AccountNumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
public class AccountNumberGeneratorImpl implements AccountNumberGenerator {

    private static final String BANK_ID = "370";
    private static final SecureRandom RNG = new SecureRandom();
    private static final Map<AccountType, Integer> TYPE_DIGIT = new EnumMap<>(AccountType.class);

    static {
        TYPE_DIGIT.put(AccountType.SAVINGS, 1);
        TYPE_DIGIT.put(AccountType.CHECKING, 2);
        TYPE_DIGIT.put(AccountType.FIXED_DEPOSIT, 3);
    }

    private final int sequenceLength;
    private final int maxRetries;
    private final AccountRepository accountRepository;

    @Autowired
    public AccountNumberGeneratorImpl(AccountRepository accountRepository,
                                      @Value("${core.account.number.sequence-length:9}") int sequenceLength,
                                      @Value("${core.account.number.max-retries:10}") int maxRetries) {
        if (sequenceLength <= 0) {
            throw new IllegalArgumentException("sequenceLength must be > 0");
        }
        this.sequenceLength = sequenceLength;
        this.maxRetries = Math.max(1, maxRetries);
        this.accountRepository = accountRepository;
    }

    @Override
    public String generate(AccountType accountType) {
        int typeDigit = resolveTypeDigit(accountType);
        log.debug("Generating account number for type {}", accountType);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String sequence = randomNumericString(sequenceLength);
            String withoutChecksum = BANK_ID + typeDigit + sequence;
            int checksum = computeLuhnChecksumDigit(withoutChecksum);
            String candidate = withoutChecksum + checksum;

            log.trace("Account number candidate {} attempt {}", candidate, attempt + 1);

            Optional<Boolean> exists = safeExistsByAccountNumber(candidate);
            if (!exists.orElse(false)) {
                log.info("Generated unique account number {} for type {}", candidate, accountType);
                return candidate;
            }
        }

        log.error("Failed to generate account number for type {} after {} attempts", accountType, maxRetries);
        throw new BusinessException("Unable to generate account number. Please retry later.");
    }

    private int resolveTypeDigit(AccountType accountType) {
        Integer digit = TYPE_DIGIT.get(accountType);
        if (digit == null) {
            log.error("Unsupported account type for number generation: {}", accountType);
            throw new BusinessException("Unsupported account type for number generation");
        }
        return digit;
    }

    private Optional<Boolean> safeExistsByAccountNumber(String accountNumber) {
    try {
        return Optional.of(accountRepository.existsByAccountNumber(accountNumber));
    } catch (Exception ex) {
        log.error("Database check failed for account number {}", accountNumber, ex);
        throw new BusinessException("Database unavailable. Cannot generate account number at the moment.");
    }
}


    private String randomNumericString(int length) {
        StringBuilder builder = new StringBuilder(length);
        IntStream.range(0, length).forEach(i -> builder.append(RNG.nextInt(10)));
        return builder.toString();
    }

    private int computeLuhnChecksumDigit(String numeric) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = numeric.length() - 1; i >= 0; i--) {
            int d = Character.digit(numeric.charAt(i), 10);
            if (doubleDigit) {
                d = d * 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        int mod = sum % 10;
        return (mod == 0) ? 0 : (10 - mod);
    }
}
