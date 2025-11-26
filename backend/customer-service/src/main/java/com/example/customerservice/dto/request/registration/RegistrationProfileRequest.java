package com.example.customerservice.dto.request.registration;

import com.example.customerservice.entity.enums.Gender;
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
public class RegistrationProfileRequest {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Full name không được để trống")
    private String fullName;

    @NotNull(message = "Date of birth không được null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender không được null")
    private Gender gender;

    @NotBlank(message = "Nationality không được để trống")
    private String nationality;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    private String occupation;

    private String position;
}
