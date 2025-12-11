package com.example.accountservice.mapper;

import com.example.accountservice.dto.request.CreateBeneficiaryRequest;
import com.example.accountservice.dto.response.BeneficiaryResponse;
import com.example.accountservice.entity.Beneficiary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BeneficiaryMapper {

    @Mapping(target = "beneficiaryId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "transferCount", constant = "0")
    @Mapping(target = "lastTransferDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Beneficiary toEntity(CreateBeneficiaryRequest request);

    BeneficiaryResponse toResponse(Beneficiary beneficiary);

    List<BeneficiaryResponse> toResponseList(List<Beneficiary> beneficiaries);

    /**
     * Update entity from request, only updating non-null fields
     */
    @Mapping(target = "beneficiaryId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "beneficiaryAccountNumber", ignore = true)
    @Mapping(target = "bankCode", ignore = true)
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "transferCount", ignore = true)
    @Mapping(target = "lastTransferDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(com.example.accountservice.dto.request.UpdateBeneficiaryRequest request, 
                     @MappingTarget Beneficiary beneficiary);
}
