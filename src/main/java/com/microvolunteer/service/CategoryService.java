package com.microvolunteer.service;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.CategoryMapper;
import com.microvolunteer.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for category operations.
 */
@Service
@Transactional
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }
    
    /**
     * Create a new category
     */
    public CategoryResponse createCategory(String name, String description) {
        logger.info("Creating new category with name: {}", name);
        
        if (categoryRepository.existsByName(name)) {
            throw BusinessException.categoryAlreadyExists(name);
        }
        
        Category category = new Category(name, description);
        Category savedCategory = categoryRepository.save(category);
        
        logger.info("Successfully created category with id: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }
    
    /**
     * Get all active categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderByNameAsc();
        return categories.stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> BusinessException.categoryNotFound(id));
        
        return categoryMapper.toResponse(category);
    }
    
    /**
     * Get category entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> BusinessException.categoryNotFound(id));
    }
    
    /**
     * Get categories by IDs (for internal use)
     */
    @Transactional(readOnly = true)
    public Set<Category> getCategoriesByIds(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Set.of();
        }
        
        Set<Category> categories = categoryIds.stream()
            .map(this::getCategoryEntityById)
            .collect(Collectors.toSet());
        
        return categories;
    }
    
    /**
     * Search categories by name
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String searchText) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchText);
        return categories.stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get categories with task count
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesWithTaskCount() {
        List<Object[]> results = categoryRepository.findCategoriesWithTaskCount();
        
        return results.stream()
            .map(result -> {
                Category category = (Category) result[0];
                Long taskCount = (Long) result[1];
                
                CategoryResponse response = categoryMapper.toResponse(category);
                response.setTaskCount(taskCount.intValue());
                return response;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Update category
     */
    public CategoryResponse updateCategory(Long id, String name, String description) {
        logger.info("Updating category with id: {}", id);
        
        Category category = getCategoryEntityById(id);
        
        // Check if name is being changed and if it already exists
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw BusinessException.categoryAlreadyExists(name);
        }
        
        category.setName(name);
        category.setDescription(description);
        Category savedCategory = categoryRepository.save(category);
        
        logger.info("Successfully updated category with id: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }
    
    /**
     * Deactivate category
     */
    public void deactivateCategory(Long id) {
        logger.info("Deactivating category with id: {}", id);
        
        Category category = getCategoryEntityById(id);
        category.setActive(false);
        categoryRepository.save(category);
        
        logger.info("Successfully deactivated category with id: {}", id);
    }
    
    /**
     * Get categories used in tasks
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesUsedInTasks() {
        List<Category> categories = categoryRepository.findCategoriesUsedInTasks();
        return categories.stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }
}
