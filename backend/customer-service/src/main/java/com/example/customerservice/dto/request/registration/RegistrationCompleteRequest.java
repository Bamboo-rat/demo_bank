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
@Schema(description = "Request hoàn tất đăng ký - Bước 5")
public class RegistrationCompleteRequest {

    @Schema(description = "Session ID nhận được từ các bước trước", example = "sess_123abc456def", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @Schema(description = "Số điện thoại đã đăng ký", example = "0912345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;
}
