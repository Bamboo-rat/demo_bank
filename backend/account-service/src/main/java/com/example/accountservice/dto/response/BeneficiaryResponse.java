package com.example.accountservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin người thụ hưởng")
public class BeneficiaryResponse {

    @Schema(description = "ID người thụ hưởng", example = "BEN-2024-0001")
    private String beneficiaryId;
    
    @Schema(description = "ID khách hàng sở hữu", example = "CUST-2024-0001")
    private String customerId;
    
    @Schema(description = "Số tài khoản người thụ hưởng", example = "ACC-2024-0002")
    private String beneficiaryAccountNumber;
    
    @Schema(description = "Tên người thụ hưởng", example = "Nguyễn Văn B")
    private String beneficiaryName;
    
    @Schema(description = "Mã ngân hàng (null nếu cùng ngân hàng)", example = "VCB")
    private String bankCode;
    
    @Schema(description = "Tên ngân hàng (null nếu cùng ngân hàng)", example = "Vietcombank")
    private String bankName;
    
    @Schema(description = "Biệt danh", example = "Anh Bình")
    private String nickname;
    
    @Schema(description = "Ghi chú", example = "Đối tác kinh doanh")
    private String note;
    
    @Schema(description = "Đã xác thực hay chưa", example = "true")
    private Boolean isVerified;
    
    @Schema(description = "Số lần đã chuyển tiền", example = "15")
    private Integer transferCount;
    
    @Schema(description = "Ngày chuyển tiền gần nhất", example = "2024-01-15T10:30:00")
    private LocalDateTime lastTransferDate;
    
    @Schema(description = "Ngày tạo", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Ngày cập nhật", example = "2024-01-15T14:20:00")
    private LocalDateTime updatedAt;

    /**
     * Indicates if this is an internal bank transfer (same bank)
     */
    public boolean isInternalTransfer() {
        return bankCode == null || bankCode.isBlank();
    }

    /**
     * Get display name (nickname if available, otherwise beneficiary name)
     */
    public String getDisplayName() {
        return (nickname != null && !nickname.isBlank()) ? nickname : beneficiaryName;
    }
}
