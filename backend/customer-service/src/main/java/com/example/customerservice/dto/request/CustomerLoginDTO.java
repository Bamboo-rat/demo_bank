package com.example.customerservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request đăng nhập hệ thống")
public class CustomerLoginDTO {
    @Schema(description = "Tên đăng nhập", example = "nguyenvana", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username không được để trống")
    private String username;

    @Schema(description = "Mật khẩu", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    @NotBlank(message = "Password không được để trống")
    private String password;
}
