package com.example.accountservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBeneficiaryRequest {

    @Size(max = 200, message = "validation.beneficiary.name.size")
    private String beneficiaryName;

    @Size(max = 100, message = "validation.beneficiary.nickname.size")
    private String nickname;

    @Size(max = 500, message = "validation.beneficiary.note.size")
    private String note;
}
