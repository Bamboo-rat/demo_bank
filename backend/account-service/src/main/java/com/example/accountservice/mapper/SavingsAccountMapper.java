package com.example.accountservice.mapper;

import com.example.accountservice.entity.FixedSavingsAccount;
import com.example.commonapi.dto.savings.SavingsBasicInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavingsAccountMapper {

    @Mapping(target = "status", expression = "java(account.getStatus().name())")
    SavingsBasicInfo toBasicInfo(FixedSavingsAccount account);

    List<SavingsBasicInfo> toBasicInfoList(List<FixedSavingsAccount> accounts);
}
