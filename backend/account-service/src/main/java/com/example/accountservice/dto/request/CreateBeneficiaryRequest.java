package com.example.accountservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request để tạo người thụ hưởng mới")
public class CreateBeneficiaryRequest {

    @Schema(description = "Số tài khoản người thụ hưởng", example = "ACC-2024-0002", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "validation.beneficiary.account.required")
    @Size(max = 20, message = "validation.beneficiary.account.size")
    private String beneficiaryAccountNumber;

    @Schema(description = "Tên người thụ hưởng", example = "Nguyễn Văn B", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "validation.beneficiary.name.required")
    @Size(max = 200, message = "validation.beneficiary.name.size")
    private String beneficiaryName;

    @Schema(description = "Mã ngân hàng (nếu là tài khoản ngân hàng khác)", example = "VCB")
    @Size(max = 20, message = "validation.beneficiary.bank.code.size")
    private String bankCode;

    @Schema(description = "Tên ngân hàng (nếu là tài khoản ngân hàng khác)", example = "Vietcombank")
    @Size(max = 200, message = "validation.beneficiary.bank.name.size")
    private String bankName;

    @Schema(description = "Biệt danh/tên gọi thân thiện", example = "Anh Bình")
    @Size(max = 100, message = "validation.beneficiary.nickname.size")
    private String nickname;

    @Schema(description = "Ghi chú về người thụ hưởng", example = "Đối tác kinh doanh")
    @Size(max = 500, message = "validation.beneficiary.note.size")
    private String note;
}
