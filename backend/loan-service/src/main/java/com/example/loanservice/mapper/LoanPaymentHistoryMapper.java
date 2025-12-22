package com.example.loanservice.mapper;

import com.example.loanservice.dto.response.LoanPaymentHistoryResponse;
import com.example.loanservice.entity.LoanPaymentHistory;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct Mapper cho Loan Payment History
 */
@Mapper(componentModel = "spring")
public interface LoanPaymentHistoryMapper {

    /**
     * Convert Entity to Response
     */
    LoanPaymentHistoryResponse toResponse(LoanPaymentHistory entity);

    /**
     * Convert List Entity to List Response
     */
    List<LoanPaymentHistoryResponse> toResponseList(List<LoanPaymentHistory> entities);
}
