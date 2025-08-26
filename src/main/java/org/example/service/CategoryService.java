package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.CategoryRequest;
import org.example.dto.CategoryResponse;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Category;
import org.example.repository.CategoryRepository;
import org.example.util.EntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;

    public Page<CategoryResponse> getAllCategories(String name, Pageable pageable) {
        Page<Category> categories;
        if (name != null && !name.trim().isEmpty()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }
        
        return categories.map(category -> {
            Long tasksCount = categoryRepository.countTasksByCategoryId(category.getId());
            return entityMapper.toCategoryResponse(category, tasksCount);
        });
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        Long tasksCount = categoryRepository.countTasksByCategoryId(id);
        return entityMapper.toCategoryResponse(category, tasksCount);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = entityMapper.toCategory(request);
        Category savedCategory = categoryRepository.save(category);
        return entityMapper.toCategoryResponse(savedCategory, 0L);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        entityMapper.updateCategoryFromRequest(category, request);
        Category updatedCategory = categoryRepository.save(category);
        Long tasksCount = categoryRepository.countTasksByCategoryId(id);
        return entityMapper.toCategoryResponse(updatedCategory, tasksCount);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Long tasksCount = categoryRepository.countTasksByCategoryId(id);
        if (tasksCount > 0) {
            throw new BadRequestException("Cannot delete category with existing tasks. " +
                    "Please reassign or delete " + tasksCount + " task(s) first.");
        }

        categoryRepository.delete(category);
    }
}