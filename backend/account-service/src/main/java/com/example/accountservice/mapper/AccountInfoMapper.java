package com.example.accountservice.mapper;

import com.example.accountservice.dto.response.AccountInfoDTO;
import com.example.accountservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AccountInfoMapper {

    @Mapping(target = "accountHolderName", ignore = true)
    @Mapping(target = "bankName", constant = "Kiên Long Bank")
    @Mapping(target = "bankCode", constant = "KLB")
    @Mapping(target = "isActive", expression = "java(account.getStatus() == com.example.accountservice.entity.enums.AccountStatus.ACTIVE)")
    AccountInfoDTO toAccountInfoDTO(Account account);
}
