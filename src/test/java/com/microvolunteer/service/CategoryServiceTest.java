package com.microvolunteer.service;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.mapper.CategoryMapper;
import com.microvolunteer.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAllCategories_ShouldReturnListOfCategoryResponses() {
        // Given
        Category category = createTestCategory();
        CategoryResponse categoryResponse = createTestCategoryResponse();
        
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(categoryResponse, result.get(0));
        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategoryResponse() {
        // Given
        Long categoryId = 1L;
        Category category = createTestCategory();
        CategoryResponse categoryResponse = createTestCategoryResponse();
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.getCategoryById(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(categoryResponse, result);
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryById_WhenCategoryDoesNotExist_ShouldThrowException() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(com.microvolunteer.exception.BusinessException.class, 
                    () -> categoryService.getCategoryById(categoryId));
        
        verify(categoryRepository).findById(categoryId);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void getActiveCategories_ShouldReturnOnlyActiveCategories() {
        // Given
        Category category = createTestCategory();
        CategoryResponse categoryResponse = createTestCategoryResponse();
        
        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When
        List<CategoryResponse> result = categoryService.getActiveCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(categoryResponse, result.get(0));
        verify(categoryRepository).findByIsActiveTrue();
        verify(categoryMapper).toResponse(category);
    }

    private Category createTestCategory() {
        return Category.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CategoryResponse createTestCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
