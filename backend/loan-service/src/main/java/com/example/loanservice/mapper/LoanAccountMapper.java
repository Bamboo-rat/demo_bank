package com.example.loanservice.mapper;

import com.example.loanservice.dto.response.LoanAccountResponse;
import com.example.loanservice.entity.LoanAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct Mapper cho Loan Account
 */
@Mapper(componentModel = "spring")
public interface LoanAccountMapper {

    /**
     * Convert Entity to Response
     */
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "daysRemaining", ignore = true)
    @Mapping(target = "installmentsPaid", ignore = true)
    @Mapping(target = "installmentsRemaining", ignore = true)
    LoanAccountResponse toResponse(LoanAccount entity);
}
