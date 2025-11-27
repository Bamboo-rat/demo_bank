package com.example.corebankingservice.service;

import com.example.corebankingservice.entity.enums.AccountType;

public interface AccountNumberGenerator {

    String generate(AccountType accountType);
}
