package com.example.customerservice.dto.response;

import com.example.customerservice.entity.enums.Gender;
import com.example.customerservice.entity.enums.KycStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response thông tin khách hàng")
public class CustomerResponse {
    @Schema(description = "ID khách hàng", example = "CUST-001")
    private String customerId;
    
    @Schema(description = "Auth Provider ID (Keycloak subject)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String authProviderId;
    
    @Schema(description = "Số CIF (Customer Information File)", example = "CIF-12345678")
    private String cifNumber;
    
    @Schema(description = "Tên đăng nhập", example = "nguyenvana")
    private String username;
    
    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;
    
    @Schema(description = "Địa chỉ email", example = "nguyenvana@example.com")
    private String email;
    
    @Schema(description = "Số điện thoại", example = "0912345678")
    private String phoneNumber;
    
    @Schema(description = "Email đã xác thực hay chưa", example = "true")
    private boolean emailVerified;
    
    @Schema(description = "Ngày sinh", example = "1990-01-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Giới tính", example = "MALE")
    private Gender gender;
    
    @Schema(description = "Địa chỉ thường trú")
    private AddressResponse permanentAddress;
    
    @Schema(description = "Địa chỉ tạm trú")
    private AddressResponse temporaryAddress;
    
    @Schema(description = "Trạng thái KYC", example = "VERIFIED")
    private KycStatus kycStatus;
    
    @Schema(description = "Thời gian tạo tài khoản", example = "2024-12-14T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật cuối", example = "2024-12-14T15:45:00")
    private LocalDateTime updatedAt;
}