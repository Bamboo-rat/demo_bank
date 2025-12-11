package com.example.accountservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBeneficiaryRequest {

    @NotBlank(message = "validation.beneficiary.account.required")
    @Size(max = 20, message = "validation.beneficiary.account.size")
    private String beneficiaryAccountNumber;

    @NotBlank(message = "validation.beneficiary.name.required")
    @Size(max = 200, message = "validation.beneficiary.name.size")
    private String beneficiaryName;

    @Size(max = 20, message = "validation.beneficiary.bank.code.size")
    private String bankCode;

    @Size(max = 200, message = "validation.beneficiary.bank.name.size")
    private String bankName;

    @Size(max = 100, message = "validation.beneficiary.nickname.size")
    private String nickname;

    @Size(max = 500, message = "validation.beneficiary.note.size")
    private String note;
}
