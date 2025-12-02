package com.example.corebankingservice.mapper;

import com.example.corebankingservice.dto.request.CreateCifRequest;
import com.example.corebankingservice.dto.response.CifResponse;
import com.example.corebankingservice.dto.response.CifStatusResponse;
import com.example.corebankingservice.entity.CIF_Master;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CifMapper {

    @Mapping(target = "cifId", ignore = true)
    @Mapping(target = "cifNumber", source = "cifNumber")
    @Mapping(target = "customerStatus", expression = "java(com.example.corebankingservice.entity.enums.CustomerStatus.ACTIVE)")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "lastTransactionDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    CIF_Master toNewEntity(CreateCifRequest request, String cifNumber);

    @Mapping(target = "accountNumber", ignore = true)
    CifResponse toResponse(CIF_Master cif);

    @Mapping(target = "accountNumber", source = "accountNumber")
    CifResponse toResponseWithAccount(CIF_Master cif, String accountNumber);

    @Mapping(target = "cifNumber", source = "cif.cifNumber")
    @Mapping(target = "customerName", source = "cif.customerName")
    @Mapping(target = "status", source = "cif.customerStatus")
    @Mapping(target = "kycStatus", source = "cif.kycStatus")
    @Mapping(target = "riskLevel", source = "cif.riskLevel")
    @Mapping(target = "canTransact", source = "canTransact")
    CifStatusResponse toStatusResponse(CIF_Master cif, boolean canTransact);
}
