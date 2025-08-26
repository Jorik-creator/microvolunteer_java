package org.example.service;

import org.example.dto.CategoryRequest;
import org.example.dto.CategoryResponse;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Category;
import org.example.repository.CategoryRepository;
import org.example.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private EntityMapper entityMapper;
    
    @InjectMocks
    private CategoryService categoryService;
    
    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    
    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .build();
        
        categoryRequest = CategoryRequest.builder()
                .name("Test Category")
                .description("Test Description")
                .build();
        
        categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .tasksCount(0L)
                .build();
    }
    
    @Test
    void getAllCategories_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(entityMapper.toCategoryResponse(category, 0L)).thenReturn(categoryResponse);
        
        Page<CategoryResponse> result = categoryService.getAllCategories(null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(categoryResponse, result.getContent().get(0));
        verify(categoryRepository).findAll(pageable);
        verify(entityMapper).toCategoryResponse(category, 0L);
    }
    
    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.countTasksByCategoryId(1L)).thenReturn(3L);
        when(entityMapper.toCategoryResponse(category, 3L)).thenReturn(categoryResponse);
        
        CategoryResponse result = categoryService.getCategoryById(1L);
        
        assertNotNull(result);
        assertEquals(categoryResponse.getId(), result.getId());
        assertEquals(categoryResponse.getName(), result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countTasksByCategoryId(1L);
        verify(entityMapper).toCategoryResponse(category, 3L);
    }
    
    @Test
    void getCategoryById_NotFound_ThrowsResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(1L)
        );
        
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).countTasksByCategoryId(any());
        verify(entityMapper, never()).toCategoryResponse(any(), any());
    }
    
    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(false);
        when(entityMapper.toCategory(categoryRequest)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(entityMapper.toCategoryResponse(category, 0L)).thenReturn(categoryResponse);
        
        CategoryResponse result = categoryService.createCategory(categoryRequest);
        
        assertNotNull(result);
        assertEquals(categoryResponse.getName(), result.getName());
        verify(categoryRepository).existsByName("Test Category");
        verify(entityMapper).toCategory(categoryRequest);
        verify(categoryRepository).save(category);
        verify(entityMapper).toCategoryResponse(category, 0L);
    }
    
    @Test
    void createCategory_NameAlreadyExists_ThrowsBadRequestException() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(true);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.createCategory(categoryRequest)
        );
        
        assertEquals("Category with name 'Test Category' already exists", exception.getMessage());
        verify(categoryRepository).existsByName("Test Category");
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void updateCategory_Success() {
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Updated Category")
                .description("Updated Description")
                .build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Updated Category")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryRepository.countTasksByCategoryId(1L)).thenReturn(2L);
        when(entityMapper.toCategoryResponse(category, 2L)).thenReturn(categoryResponse);
        
        CategoryResponse result = categoryService.updateCategory(1L, updateRequest);
        
        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Updated Category");
        verify(entityMapper).updateCategoryFromRequest(category, updateRequest);
        verify(categoryRepository).save(category);
        verify(categoryRepository).countTasksByCategoryId(1L);
        verify(entityMapper).toCategoryResponse(category, 2L);
    }
    
    @Test
    void updateCategory_NotFound_ThrowsResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(1L, categoryRequest)
        );
        
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void updateCategory_NameAlreadyExists_ThrowsBadRequestException() {
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Existing Category")
                .description("Updated Description")
                .build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Existing Category")).thenReturn(true);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.updateCategory(1L, updateRequest)
        );
        
        assertEquals("Category with name 'Existing Category' already exists", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Existing Category");
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void updateCategory_SameName_Success() {
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Test Category") // Same as existing
                .description("Updated Description")
                .build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryRepository.countTasksByCategoryId(1L)).thenReturn(1L);
        when(entityMapper.toCategoryResponse(category, 1L)).thenReturn(categoryResponse);
        
        CategoryResponse result = categoryService.updateCategory(1L, updateRequest);
        
        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).existsByName(any()); // Should not check for existing name
        verify(entityMapper).updateCategoryFromRequest(category, updateRequest);
        verify(categoryRepository).save(category);
        verify(categoryRepository).countTasksByCategoryId(1L);
        verify(entityMapper).toCategoryResponse(category, 1L);
    }
    
    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.countTasksByCategoryId(1L)).thenReturn(0L);
        
        categoryService.deleteCategory(1L);
        
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countTasksByCategoryId(1L);
        verify(categoryRepository).delete(category);
    }
    
    @Test
    void deleteCategory_NotFound_ThrowsResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(1L)
        );
        
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).delete(any());
    }
    
    @Test
    void deleteCategory_HasTasks_ThrowsBadRequestException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.countTasksByCategoryId(1L)).thenReturn(3L);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(1L)
        );
        
        assertEquals("Cannot delete category with existing tasks. Please reassign or delete 3 task(s) first.", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countTasksByCategoryId(1L);
        verify(categoryRepository, never()).delete(any());
    }
}