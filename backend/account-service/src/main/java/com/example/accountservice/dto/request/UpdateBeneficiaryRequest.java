package com.example.accountservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request để cập nhật thông tin người thụ hưởng")
public class UpdateBeneficiaryRequest {

    @Schema(description = "Tên người thụ hưởng", example = "Nguyễn Văn B - Updated")
    @Size(max = 200, message = "validation.beneficiary.name.size")
    private String beneficiaryName;

    @Schema(description = "Biệt danh/tên gọi thân thiện", example = "Anh Bình - Cập nhật")
    @Size(max = 100, message = "validation.beneficiary.nickname.size")
    private String nickname;

    @Schema(description = "Ghi chú về người thụ hưởng", example = "Cập nhật thông tin đối tác")
    @Size(max = 500, message = "validation.beneficiary.note.size")
    private String note;
}
