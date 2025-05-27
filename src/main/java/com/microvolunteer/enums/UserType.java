package com.microvolunteer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    VOLUNTEER("Волонтер"),
    VULNERABLE("Вразлива людина");

    private final String description;
}