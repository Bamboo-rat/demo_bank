package com.example.accountservice.service.impl;

import com.example.accountservice.exception.AccountNumberGenerationException;
import com.example.accountservice.exception.InvalidAccountTypeException;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.service.AccountNumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Sinh số tài khoản theo format:
 * [BankId(3)][AccountType(1)][Sequence(N)][Checksum(1)]
 *
 * BankId = "370"
 * AccountType = 1..4
 * Sequence = pseudo-random digits
 * Checksum = Luhn (mod 10) computed from all previous digits
 */
@Service
@Slf4j
public class AccountNumberGeneratorImpl implements AccountNumberGenerator {

    private static final String BANK_ID = "370";
    private static final SecureRandom RNG = new SecureRandom();

    // Độ dài của phần 'số thứ tự' (có thể chỉnh). Tổng độ dài = 3 + 1 + SEQ_LEN + 1
    private final int sequenceLength;

    // Số lần thử khi gặp collision với DB trước khi báo lỗi
    private final int maxRetries;

    private final AccountRepository accountRepository; // optional, để kiểm tra trùng

    @Autowired
    public AccountNumberGeneratorImpl(AccountRepository accountRepository) {
        this(accountRepository, 9, 10);
    }

    public AccountNumberGeneratorImpl(AccountRepository accountRepository, int sequenceLength, int maxRetries) {
        if (sequenceLength <= 0) throw new IllegalArgumentException("sequenceLength must be > 0");
        this.sequenceLength = sequenceLength;
        this.maxRetries = Math.max(1, maxRetries);
        this.accountRepository = accountRepository;
    }

    /**
     * Sinh số tài khoản cho loại accountType (1..4).
     * Kiểm tra unique bằng accountRepository nếu repository được cung cấp (không null).
     *
     * @param accountTypeDigit integer 1..4
     * @return số tài khoản (String)
     */
    public String generate(int accountTypeDigit) throws InvalidAccountTypeException, AccountNumberGenerationException {
        validateAccountTypeDigit(accountTypeDigit);

        log.debug("Generating account number for account type: {}", accountTypeDigit);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String seq = randomNumericString(sequenceLength);
            String withoutChecksum = BANK_ID + accountTypeDigit + seq;
            int checksum = computeLuhnChecksumDigit(withoutChecksum);
            String accNumber = withoutChecksum + checksum;

            log.debug("Generated account number candidate: {} (attempt {})", accNumber, attempt + 1);

            // nếu repository null (không inject), bỏ qua check unique
            if (accountRepository == null) {
                log.debug("Repository is null, returning account number without uniqueness check");
                return accNumber;
            }

            Optional<Boolean> exists = safeExistsByAccountNumber(accNumber);
            if (!exists.orElse(false)) {
                log.info("Successfully generated unique account number: {}", accNumber);
                return accNumber;
            }
            log.debug("Account number {} already exists, retrying...", accNumber);
        }

        log.error("Failed to generate unique account number after {} attempts", maxRetries);
        throw new AccountNumberGenerationException(maxRetries);
    }

    public void validateAccountTypeDigit(int accountTypeDigit) throws InvalidAccountTypeException {
        if (accountTypeDigit < 1 || accountTypeDigit > 4) {
            log.warn("Invalid account type digit provided: {}", accountTypeDigit);
            throw new InvalidAccountTypeException(accountTypeDigit);
        }
    }

    public Optional<Boolean> safeExistsByAccountNumber(String accountNumber) {
        try {
            Boolean exists = accountRepository.existsByAccountNumber(accountNumber);
            return Optional.ofNullable(exists);
        } catch (Exception e) {
            log.error("Database error while checking account number existence: {}", accountNumber, e);
            return Optional.empty();
        }
    }

    /**
     * Sinh chuỗi chỉ gồm chữ số length ký tự
     */
    public String randomNumericString(int length) {
        StringBuilder sb = new StringBuilder(length);
        IntStream.range(0, length).forEach(i -> sb.append(RNG.nextInt(10)));
        return sb.toString();
    }

    /**
     * Tính chữ số checksum theo thuật toán Luhn:
     * trả về 0..9 là chữ số cần thêm để tổng Luhn % 10 == 0
     */
    public Integer computeLuhnChecksumDigit(String numeric) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = numeric.length() - 1; i >= 0; i--) {
            int d = Character.digit(numeric.charAt(i), 10);
            if (doubleDigit) {
                d = d * 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        // tính digit để (sum + checksum) % 10 == 0
        int mod = sum % 10;
        return (mod == 0) ? 0 : (10 - mod);
    }

    /**
     * Kiểm tra valid theo Luhn (dùng để validate input số tài khoản)
     */
    public Boolean isValidLuhn(String fullNumeric) {
        if (Objects.isNull(fullNumeric) || !fullNumeric.matches("\\d+")) return false;
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = fullNumeric.length() - 1; i >= 0; i--) {
            int d = Character.digit(fullNumeric.charAt(i), 10);
            if (doubleDigit) {
                d = d * 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
