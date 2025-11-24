package com.example.accountservice.service;

import com.example.accountservice.exception.AccountNumberGenerationException;
import com.example.accountservice.exception.InvalidAccountTypeException;

import java.util.Optional;

public interface AccountNumberGenerator {
    /**
     * Generates a unique account number for the specified account type.
     *
     * @param accountTypeDigit The account type digit (1-4)
     * @return Generated account number
     * @throws InvalidAccountTypeException if accountTypeDigit is not in range [1..4]
     * @throws AccountNumberGenerationException if unable to generate unique number after max retries
     */
    String generate(int accountTypeDigit) throws InvalidAccountTypeException, AccountNumberGenerationException;

    /**
     * Validates that the account type digit is within the valid range [1..4].
     *
     * @param accountTypeDigit The account type digit to validate
     * @throws InvalidAccountTypeException if the digit is not in valid range
     */
    void validateAccountTypeDigit(int accountTypeDigit) throws InvalidAccountTypeException;

    /**
     * Safely checks if an account number exists in the database.
     *
     * @param accountNumber The account number to check
     * @return Optional containing true if exists, false if not, empty if database error
     */
    Optional<Boolean> safeExistsByAccountNumber(String accountNumber);

    /**
     * Generates a random numeric string of specified length.
     *
     * @param length The length of the string to generate
     * @return Random numeric string
     */
    String randomNumericString(int length);

    /**
     * Computes the Luhn checksum digit for a numeric string.
     *
     * @param numeric The numeric string to compute checksum for
     * @return The checksum digit (0-9)
     */
    Integer computeLuhnChecksumDigit(String numeric);

    /**
     * Validates if a full numeric string is valid according to Luhn algorithm.
     *
     * @param fullNumeric The complete numeric string including checksum
     * @return true if valid, false otherwise
     */
    Boolean isValidLuhn(String fullNumeric);
}
