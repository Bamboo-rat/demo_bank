package com.example.customerservice.dto.request;

import com.example.customerservice.entity.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRegisterRequest {

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

    @NotBlank(message = "National ID không được để trống")
    private String nationalId;

    @NotNull(message = "Issue date of national ID không được null")
    @PastOrPresent(message = "Issue date must be in the past or present")
    private LocalDate issueDateNationalId;

    @NotBlank(message = "Place of issue for national ID không được để trống")
    private String placeOfIssueNationalId;

    private String occupation;

    private String position;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @Valid // Important: Quê quán
    @NotNull(message = "Permanent address cannot be null")
    private AddressRequest permanentAddress;

    @Valid
    private AddressRequest temporaryAddress; // Tạm trú
}
