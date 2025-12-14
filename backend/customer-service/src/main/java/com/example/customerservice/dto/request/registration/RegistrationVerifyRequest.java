package com.example.customerservice.dto.request.registration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request xác thực OTP - Bước 2")
public class RegistrationVerifyRequest {

    @Schema(description = "Số điện thoại đã đăng ký ở bước 1", example = "0912345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "Mã OTP 6 chữ số nhận được qua SMS", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "^\\d{6}$")
    @NotBlank(message = "OTP cannot be blank")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit number")
    private String otp;
}
