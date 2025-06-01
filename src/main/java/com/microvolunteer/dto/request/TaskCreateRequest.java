package com.microvolunteer.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Назва завдання є обов'язковою")
    @Size(max = 255, message = "Назва завдання не може перевищувати 255 символів")
    private String title;

    @NotBlank(message = "Опис завдання є обов'язковим")
    private String description;

    @NotNull(message = "Категорія є обов'язковою")
    private Long categoryId;

    @NotBlank(message = "Місце проведення є обов'язковим")
    @Size(max = 255, message = "Місце проведення не може перевищувати 255 символів")
    private String location;

    @Size(max = 500, message = "Необхідні навички не можуть перевищувати 500 символів")
    private String requiredSkills;

    @NotNull(message = "Час виконання є обов'язковим")
    @Future(message = "Час виконання має бути в майбутньому")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledAt;

    @NotNull(message = "Кількість учасників є обов'язковою")
    @Min(value = 1, message = "Кількість учасників має бути мінімум 1")
    @Max(value = 100, message = "Кількість учасників не може перевищувати 100")
    private Integer maxParticipants;
}