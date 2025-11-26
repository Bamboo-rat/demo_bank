package com.example.customerservice.redis.model;

import com.example.customerservice.entity.enums.Gender;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationProfileData implements Serializable {

    private static final long serialVersionUID = -4483497136568288298L;

    private String password;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String nationality;
    private String email;
    private String occupation;
    private String position;
}
