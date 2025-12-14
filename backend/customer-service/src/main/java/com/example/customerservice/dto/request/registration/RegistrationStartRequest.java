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
@Schema(description = "Request bắt đầu đăng ký - Bước 1")
public class RegistrationStartRequest {

    @Schema(
        description = "Số điện thoại (định dạng VN). OTP sẽ được gửi đến số này",
        example = "0912345678",
        requiredMode = Schema.RequiredMode.REQUIRED,
        pattern = "^(0|\\+84)[\\s\\./0-9]*$"
    )
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;
}
