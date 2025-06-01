package com.microvolunteer.controller;

import com.microvolunteer.dto.request.CategoryCreateRequest;
import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.mapper.CategoryMapper;
import com.microvolunteer.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "Get all categories", description = "Retrieve all categories in the system")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("Request to get all categories");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Get active categories", description = "Retrieve only active categories")
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        log.info("Request to get active categories");
        List<CategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        log.info("Request to get category with ID: {}", id);
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("Category not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create new category", description = "Create a new category (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        log.info("Request to create new category: {}", request.getName());
        
        try {
            Category category = categoryMapper.toEntity(request);
            CategoryResponse response = categoryService.createCategory(category);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update category", description = "Update an existing category (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, 
                                                          @Valid @RequestBody CategoryCreateRequest request) {
        log.info("Request to update category with ID: {}", id);
        
        try {
            Category updatedCategory = categoryMapper.toEntity(request);
            CategoryResponse response = categoryService.updateCategory(id, updatedCategory)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating category: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete category", description = "Delete a category (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Request to delete category with ID: {}", id);
        
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Deactivate category", description = "Deactivate a category (Admin only)")
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        log.info("Request to deactivate category with ID: {}", id);
        
        try {
            boolean deactivated = categoryService.deactivateCategory(id);
            if (deactivated) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deactivating category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
