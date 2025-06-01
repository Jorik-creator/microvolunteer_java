package com.microvolunteer.service;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.CategoryMapper;
import com.microvolunteer.repository.CategoryRepository;
import com.microvolunteer.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final TaskRepository taskRepository;

    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Category not found with id: " + id));
    }



    public List<CategoryResponse> getActiveCategories() {
        log.info("Fetching active categories");
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(com.microvolunteer.dto.request.CategoryCreateRequest request) {
        log.info("Creating new category from request: {}", request.getName());
        
        Category category = categoryMapper.toEntity(request);
        validateCategoryCreation(category);
        
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, com.microvolunteer.dto.request.CategoryCreateRequest request) {
        log.info("Updating category with ID: {} from request", id);
        
        return categoryRepository.findById(id)
                .map(category -> {
                    Category updatedCategory = categoryMapper.toEntity(request);
                    validateCategoryUpdate(category, updatedCategory);
                    
                    if (updatedCategory.getName() != null) {
                        category.setName(updatedCategory.getName());
                    }
                    if (updatedCategory.getDescription() != null) {
                        category.setDescription(updatedCategory.getDescription());
                    }
                    
                    category.setUpdatedAt(LocalDateTime.now());
                    Category savedCategory = categoryRepository.save(category);
                    return categoryMapper.toResponse(savedCategory);
                })
                .orElseThrow(() -> new BusinessException("Category not found with id: " + id));
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        
        return categoryRepository.findById(id)
                .map(category -> {
                    validateCategoryDeletion(category);
                    
                    categoryRepository.delete(category);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean deactivateCategory(Long id) {
        log.info("Deactivating category with ID: {}", id);
        
        return categoryRepository.findById(id)
                .map(category -> {
                    if (!category.getIsActive()) {
                        throw new BusinessException("Category is already inactive");
                    }
                    
                    category.setIsActive(false);
                    category.setUpdatedAt(LocalDateTime.now());
                    categoryRepository.save(category);
                    return true;
                })
                .orElse(false);
    }

    private void validateCategoryCreation(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("Category name cannot be empty");
        }
        
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new BusinessException("Category with this name already exists");
        }
        
        if (category.getName().length() > 100) {
            throw new BusinessException("Category name cannot exceed 100 characters");
        }
    }

    private void validateCategoryUpdate(Category existingCategory, Category updatedCategory) {
        if (!existingCategory.getIsActive()) {
            throw new BusinessException("Cannot update inactive category");
        }
        
        if (updatedCategory.getName() != null && 
            !updatedCategory.getName().equals(existingCategory.getName())) {
            
            if (categoryRepository.findByName(updatedCategory.getName()).isPresent()) {
                throw new BusinessException("Category with this name already exists");
            }
            
            if (updatedCategory.getName().length() > 100) {
                throw new BusinessException("Category name cannot exceed 100 characters");
            }
        }
    }

    private void validateCategoryDeletion(Category category) {
        List<com.microvolunteer.entity.Task> tasks = taskRepository.findByCategoryId(category.getId());
        if (!tasks.isEmpty()) {
            throw new BusinessException("Cannot delete category that has tasks");
        }
    }
}
