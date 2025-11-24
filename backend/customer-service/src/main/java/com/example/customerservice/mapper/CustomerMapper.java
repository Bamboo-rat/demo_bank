package com.example.customerservice.mapper;

import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.entity.Customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {AddressMapper.class}
)
public interface CustomerMapper {

    @Mapping(source = "phoneNumber", target = "username")
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "authProviderId", ignore = true) 
    @Mapping(target = "coreBankingId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "kycStatus", ignore = true)
    @Mapping(target = "riskLevel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRegisterRequest dto);


    CustomerResponse toResponse(Customer entity);
}