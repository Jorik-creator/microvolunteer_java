package com.microvolunteer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    OPEN("Відкрито"),
    IN_PROGRESS("В процесі"),
    COMPLETED("Завершено"),
    CANCELLED("Скасовано");

    private final String description;
}