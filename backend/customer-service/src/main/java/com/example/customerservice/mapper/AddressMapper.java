package com.example.customerservice.mapper;

import com.example.customerservice.dto.request.AddressRequest;
import com.example.customerservice.dto.response.AddressResponse;
import com.example.customerservice.entity.Address;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddressRequest addressDTO);
    AddressResponse toResponse(Address entity);
}