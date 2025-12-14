package com.example.customerservice.dto.request;

import com.example.customerservice.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request đăng ký khách hàng mới")
public class CustomerRegisterRequest {

    @Schema(description = "Mật khẩu đăng nhập (tối thiểu 8 ký tự)", example = "password123", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "Số CMND/CCCD", example = "001234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "National ID không được để trống")
    private String nationalId;

    @Schema(description = "Ngày cấp CMND/CCCD", example = "2020-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Issue date of national ID không được null")
    @PastOrPresent(message = "Issue date must be in the past or present")
    private LocalDate issueDateNationalId;

    @Schema(description = "Nơi cấp CMND/CCCD", example = "Công an TP. Hà Nội", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Place of issue for national ID không được để trống")
    private String placeOfIssueNationalId;

    @Schema(description = "Nghề nghiệp", example = "Kỹ sư phần mềm")
    private String occupation;

    @Schema(description = "Vị trí công việc", example = "Senior Developer")
    private String position;

    @Schema(description = "Địa chỉ email", example = "nguyenvana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Số điện thoại (định dạng VN: 0xxxxxxxxx hoặc +84xxxxxxxxx)", example = "0912345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "Địa chỉ thường trú (quê quán)", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "Permanent address cannot be null")
    private AddressRequest permanentAddress;

    @Schema(description = "Địa chỉ tạm trú (nếu có)")
    @Valid
    private AddressRequest temporaryAddress;
}
