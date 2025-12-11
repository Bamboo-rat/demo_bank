package com.example.corebankingservice.mapper;

import com.example.corebankingservice.dto.request.OpenAccountCoreRequest;
import com.example.corebankingservice.dto.response.AccountDetailResponse;
import com.example.corebankingservice.dto.response.AccountStatusHistoryResponse;
import com.example.corebankingservice.dto.response.AccountStatusResponse;
import com.example.corebankingservice.dto.response.BalanceResponse;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.AccountStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "balance", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "holdAmount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "amlFlag", constant = "false")
    @Mapping(target = "openedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cifNumber", source = "cifNumber")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "accountType", source = "accountType")
    Account toEntity(OpenAccountCoreRequest request);

    AccountDetailResponse toDetail(Account account);

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "status", source = "account.status")
    @Mapping(target = "amlFlag", source = "account.amlFlag")
    @Mapping(target = "updatedAt", source = "account.updatedAt")
    AccountStatusResponse toStatus(Account account);

    AccountStatusHistoryResponse toHistory(AccountStatusHistory history);

    @Mapping(target = "availableBalance", expression = "java(account.getBalance().subtract(account.getHoldAmount()))")
    @Mapping(target = "checkedAt", expression = "java(java.time.LocalDateTime.now())")
    BalanceResponse toBalance(Account account);
}
