package com.example.customerservice.mapper;

import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.entity.Customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CustomerMapper {

    @Mapping(source = "phoneNumber", target = "username")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "nationalId", target = "nationalId")
    @Mapping(source = "permanentAddress", target = "permanentAddress")
    @Mapping(source = "temporaryAddress", target = "temporaryAddress")
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "authProviderId", ignore = true)
    @Mapping(target = "cifNumber", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "kycStatus", expression = "java(com.example.customerservice.entity.enums.KycStatus.PENDING)")
    Customer toEntity(CustomerRegisterRequest dto);

    CustomerResponse toResponse(Customer entity);

    @Mapping(target = "customerStatus", expression = "java(entity.getKycStatus().name())")
    com.example.commonapi.dto.customer.CustomerBasicInfo toBasicInfo(Customer entity);
}