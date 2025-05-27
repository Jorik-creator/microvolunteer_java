package com.microvolunteer.controller;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Категорії", description = "API для роботи з категоріями завдань")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Отримати всі категорії")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорій отримано")
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("Отримання всіх категорій");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}