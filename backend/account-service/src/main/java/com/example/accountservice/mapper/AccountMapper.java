package com.example.accountservice.mapper;

import com.example.accountservice.dto.response.AccountResponse;
import com.example.accountservice.dto.response.CreditAccountResponse;
import com.example.accountservice.dto.response.SavingsAccountResponse;
import com.example.accountservice.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {


    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerStatus", ignore = true)
    @Mapping(target = "cifNumber", ignore = true)
    AccountResponse toResponse(Account account);

    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerStatus", ignore = true)
    @Mapping(target = "cifNumber", ignore = true)
    AccountResponse toResponse(CheckingAccount checkingAccount);

    List<AccountResponse> toResponseList(List<? extends Account> accounts);

    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerStatus", ignore = true)
    @Mapping(target = "cifNumber", ignore = true)
    @Mapping(target = "projectedInterest", ignore = true) // Calculated separately
    SavingsAccountResponse toSavingsResponse(SavingsAccount savingsAccount);


    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerStatus", ignore = true)
    @Mapping(target = "cifNumber", ignore = true)
    CreditAccountResponse toCreditResponse(CreditAccount creditAccount);
}