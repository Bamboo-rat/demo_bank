package com.example.loanservice.mapper;

import com.example.loanservice.dto.response.RepaymentScheduleResponse;
import com.example.loanservice.entity.RepaymentSchedule;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct Mapper cho Repayment Schedule
 */
@Mapper(componentModel = "spring")
public interface RepaymentScheduleMapper {

    /**
     * Convert Entity to Response
     */
    RepaymentScheduleResponse toResponse(RepaymentSchedule entity);

    /**
     * Convert List Entity to List Response
     */
    List<RepaymentScheduleResponse> toResponseList(List<RepaymentSchedule> entities);
}
