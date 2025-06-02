package com.microvolunteer.controller;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for category operations.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "Operations related to task category management")
@SecurityRequirement(name = "keycloak")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @Operation(
        summary = "Get all active categories",
        description = "Retrieve all active categories available for tasks"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    @Operation(
        summary = "Get category by ID",
        description = "Retrieve a specific category by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Search categories",
        description = "Search categories by name"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(
            @Parameter(description = "Search text") @RequestParam String searchText) {
        
        List<CategoryResponse> categories = categoryService.searchCategories(searchText);
        return ResponseEntity.ok(categories);
    }
    
    @Operation(
        summary = "Get categories with task count",
        description = "Retrieve categories along with the number of tasks in each category"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories with task count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/with-task-count")
    public ResponseEntity<List<CategoryResponse>> getCategoriesWithTaskCount() {
        List<CategoryResponse> categories = categoryService.getCategoriesWithTaskCount();
        return ResponseEntity.ok(categories);
    }
    
    @Operation(
        summary = "Get categories used in tasks",
        description = "Retrieve only categories that are currently used in at least one task"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Used categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/used")
    public ResponseEntity<List<CategoryResponse>> getCategoriesUsedInTasks() {
        List<CategoryResponse> categories = categoryService.getCategoriesUsedInTasks();
        return ResponseEntity.ok(categories);
    }
    
    @Operation(
        summary = "Create a new category",
        description = "Create a new task category. Only admins can create categories."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only admins can create categories"),
        @ApiResponse(responseCode = "409", description = "Category name already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Update a category",
        description = "Update an existing category. Only admins can update categories."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only admins can update categories"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category name already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request) {
        
        CategoryResponse response = categoryService.updateCategory(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Deactivate a category",
        description = "Deactivate a category. Only admins can deactivate categories."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deactivated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only admins can deactivate categories"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        
        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DTO for category creation request
     */
    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
        private String name;
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        public CreateCategoryRequest() {}
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    /**
     * DTO for category update request
     */
    public static class UpdateCategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
        private String name;
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        public UpdateCategoryRequest() {}
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
