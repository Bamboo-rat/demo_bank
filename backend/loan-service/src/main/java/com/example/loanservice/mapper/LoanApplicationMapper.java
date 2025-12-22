package com.example.loanservice.mapper;

import com.example.loanservice.dto.request.LoanApplicationRequest;
import com.example.loanservice.dto.response.LoanApplicationResponse;
import com.example.loanservice.entity.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct Mapper cho Loan Application
 */
@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {

    /**
     * Convert Request to Entity
     */
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "scoringResult", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoanApplication toEntity(LoanApplicationRequest request);

    /**
     * Convert Entity to Response
     */
    LoanApplicationResponse toResponse(LoanApplication entity);

    /**
     * Update entity from Request (partial update)
     */
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(LoanApplicationRequest request, @MappingTarget LoanApplication entity);
}
