package com.example.customerservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("gender.male"),
    FEMALE("gender.female"),
    OTHER("gender.other");

    private final String messageCode;
}
