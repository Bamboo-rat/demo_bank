package com.example.corebankingservice.mapper;

import com.example.corebankingservice.dto.loan.LoanInfoResponse;
import com.example.corebankingservice.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanMapper {

    LoanInfoResponse toInfoResponse(Loan loan);
}
