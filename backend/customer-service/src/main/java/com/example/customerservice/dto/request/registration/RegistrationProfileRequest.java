package com.example.customerservice.dto.request.registration;

import com.example.customerservice.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request lưu thông tin cá nhân - Bước 3")
public class RegistrationProfileRequest {

    @Schema(description = "Session ID nhận được từ bước xác thực OTP", example = "sess_123abc456def", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @Schema(description = "Số điện thoại đã đăng ký", example = "0912345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "Mật khẩu đăng nhập (tối thiểu 8 ký tự)", example = "password123", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn A", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name không được để trống")
    private String fullName;

    @Schema(description = "Ngày sinh", example = "1990-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Date of birth không được null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Gender không được null")
    private Gender gender;

    @Schema(description = "Quốc tịch", example = "Việt Nam", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nationality không được để trống")
    private String nationality;

    @Schema(description = "Địa chỉ email", example = "nguyenvana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Nghề nghiệp", example = "Kỹ sư phần mềm")
    private String occupation;

    @Schema(description = "Vị trí công việc", example = "Senior Developer")
    private String position;
}
