package com.example.accountservice.dto.response;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO chứa thông tin cơ bản tài khoản (dùng cho internal API)")
public class AccountInfoDTO {
    
    @Schema(description = "Số tài khoản", example = "ACC-2024-0001")
    private String accountNumber;
    
    @Schema(description = "Tên chủ tài khoản", example = "Nguyễn Văn A")
    private String accountHolderName; // Tên chủ tài khoản
    
    @Schema(description = "Loại tài khoản", example = "CHECKING")
    private AccountType accountType;
    
    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private AccountStatus status;
    
    @Schema(description = "Tên ngân hàng", example = "KLB Bank")
    private String bankName; // Tên ngân hàng
    
    @Schema(description = "Mã ngân hàng", example = "KLB")
    private String bankCode; // Mã ngân hàng

    @Schema(description = "Mã CIF (Customer Information File)", example = "CIF-2024-0001")
    private String cifNumber; // Mã CIF
    
    @Schema(description = "Tài khoản có thể nhận tiền hay không", example = "true")
    private Boolean isActive; // Trạng thái có thể nhận tiền không
}
