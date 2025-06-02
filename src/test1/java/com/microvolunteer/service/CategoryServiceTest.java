package com.microvolunteer.service;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.CategoryMapper;
import com.microvolunteer.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        category = createCategory(1L, "Test Category", "Test Description", true);
        
        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Test Category");
        categoryResponse.setDescription("Test Description");
        categoryResponse.setActive(true);
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            // Given
            String name = "New Category";
            String description = "New Description";
            
            when(categoryRepository.existsByName(name)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            CategoryResponse result = categoryService.createCategory(name, description);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Category", result.getName());
            
            verify(categoryRepository).existsByName(name);
            verify(categoryRepository).save(any(Category.class));
            verify(categoryMapper).toResponse(category);
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowExceptionWhenCategoryNameExists() {
            // Given
            String existingName = "Existing Category";
            when(categoryRepository.existsByName(existingName)).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.createCategory(existingName, "Description"));

            assertEquals("CATEGORY_ALREADY_EXISTS", exception.getCode());
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should get all active categories successfully")
        void shouldGetAllActiveCategoriesSuccessfully() {
            // Given
            List<Category> categories = List.of(category);
            when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(categories);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            List<CategoryResponse> result = categoryService.getAllActiveCategories();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            verify(categoryRepository).findByActiveTrueOrderByNameAsc();
            verify(categoryMapper).toResponse(category);
        }

        @Test
        @DisplayName("Should return empty list when no active categories")
        void shouldReturnEmptyListWhenNoActiveCategories() {
            // Given
            when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of());

            // When
            List<CategoryResponse> result = categoryService.getAllActiveCategories();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository).findByActiveTrueOrderByNameAsc();
        }

        @Test
        @DisplayName("Should get category by ID successfully")
        void shouldGetCategoryByIdSuccessfully() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            CategoryResponse result = categoryService.getCategoryById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Category", result.getName());
            verify(categoryRepository).findById(1L);
            verify(categoryMapper).toResponse(category);
        }

        @Test
        @DisplayName("Should throw exception when category not found by ID")
        void shouldThrowExceptionWhenCategoryNotFoundById() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.getCategoryById(1L));

            assertEquals("CATEGORY_NOT_FOUND", exception.getCode());
        }

        @Test
        @DisplayName("Should get category entity by ID successfully")
        void shouldGetCategoryEntityByIdSuccessfully() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            // When
            Category result = categoryService.getCategoryEntityById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Category", result.getName());
            verify(categoryRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("Get Categories by IDs Tests")
    class GetCategoriesByIdsTests {

        @Test
        @DisplayName("Should get categories by IDs successfully")
        void shouldGetCategoriesByIdsSuccessfully() {
            // Given
            Category category2 = createCategory(2L, "Category 2", "Description 2", true);
            Set<Long> categoryIds = Set.of(1L, 2L);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));

            // When
            Set<Category> result = categoryService.getCategoriesByIds(categoryIds);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).findById(2L);
        }

        @Test
        @DisplayName("Should return empty set when category IDs is null")
        void shouldReturnEmptySetWhenCategoryIdsIsNull() {
            // When
            Set<Category> result = categoryService.getCategoriesByIds(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should return empty set when category IDs is empty")
        void shouldReturnEmptySetWhenCategoryIdsIsEmpty() {
            // When
            Set<Category> result = categoryService.getCategoriesByIds(Set.of());

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when one of the categories not found")
        void shouldThrowExceptionWhenOneOfCategoriesNotFound() {
            // Given
            Set<Long> categoryIds = Set.of(1L, 999L);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.getCategoriesByIds(categoryIds));

            assertEquals("CATEGORY_NOT_FOUND", exception.getCode());
        }
    }

    @Nested
    @DisplayName("Search Categories Tests")
    class SearchCategoriesTests {

        @Test
        @DisplayName("Should search categories successfully")
        void shouldSearchCategoriesSuccessfully() {
            // Given
            String searchText = "Test";
            List<Category> categories = List.of(category);
            when(categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchText))
                    .thenReturn(categories);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            List<CategoryResponse> result = categoryService.searchCategories(searchText);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Category", result.get(0).getName());
            verify(categoryRepository).findByNameContainingIgnoreCaseAndActiveTrue(searchText);
        }

        @Test
        @DisplayName("Should return empty list when no categories match search")
        void shouldReturnEmptyListWhenNoCategoriesMatchSearch() {
            // Given
            String searchText = "NonExistent";
            when(categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchText))
                    .thenReturn(List.of());

            // When
            List<CategoryResponse> result = categoryService.searchCategories(searchText);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Categories with Task Count Tests")
    class CategoriesWithTaskCountTests {

        @Test
        @DisplayName("Should get categories with task count successfully")
        void shouldGetCategoriesWithTaskCountSuccessfully() {
            // Given
            Object[] result1 = {category, 5L};
            List<Object[]> repositoryResults = List.of(result1);
            
            when(categoryRepository.findCategoriesWithTaskCount()).thenReturn(repositoryResults);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            List<CategoryResponse> result = categoryService.getCategoriesWithTaskCount();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getTaskCount());
            verify(categoryRepository).findCategoriesWithTaskCount();
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            // Given
            String newName = "Updated Category";
            String newDescription = "Updated Description";
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName(newName)).thenReturn(false);
            when(categoryRepository.save(category)).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            CategoryResponse result = categoryService.updateCategory(1L, newName, newDescription);

            // Then
            assertNotNull(result);
            assertEquals(newName, category.getName());
            assertEquals(newDescription, category.getDescription());
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should update category with same name successfully")
        void shouldUpdateCategoryWithSameNameSuccessfully() {
            // Given
            String sameName = category.getName();
            String newDescription = "Updated Description";
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.save(category)).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            CategoryResponse result = categoryService.updateCategory(1L, sameName, newDescription);

            // Then
            assertNotNull(result);
            assertEquals(sameName, category.getName());
            assertEquals(newDescription, category.getDescription());
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(category);
            // Should not check for existing name when name hasn't changed
            verify(categoryRepository, never()).existsByName(sameName);
        }

        @Test
        @DisplayName("Should throw exception when updating to existing name")
        void shouldThrowExceptionWhenUpdatingToExistingName() {
            // Given
            String existingName = "Existing Category";
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName(existingName)).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(1L, existingName, "Description"));

            assertEquals("CATEGORY_ALREADY_EXISTS", exception.getCode());
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when category not found for update")
        void shouldThrowExceptionWhenCategoryNotFoundForUpdate() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.updateCategory(1L, "New Name", "New Description"));

            assertEquals("CATEGORY_NOT_FOUND", exception.getCode());
        }
    }

    @Nested
    @DisplayName("Deactivate Category Tests")
    class DeactivateCategoryTests {

        @Test
        @DisplayName("Should deactivate category successfully")
        void shouldDeactivateCategorySuccessfully() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.save(category)).thenReturn(category);

            // When
            categoryService.deactivateCategory(1L);

            // Then
            assertFalse(category.getActive());
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw exception when category not found for deactivation")
        void shouldThrowExceptionWhenCategoryNotFoundForDeactivation() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.deactivateCategory(1L));

            assertEquals("CATEGORY_NOT_FOUND", exception.getCode());
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Categories Used in Tasks Tests")
    class CategoriesUsedInTasksTests {

        @Test
        @DisplayName("Should get categories used in tasks successfully")
        void shouldGetCategoriesUsedInTasksSuccessfully() {
            // Given
            List<Category> categories = List.of(category);
            when(categoryRepository.findCategoriesUsedInTasks()).thenReturn(categories);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            // When
            List<CategoryResponse> result = categoryService.getCategoriesUsedInTasks();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            verify(categoryRepository).findCategoriesUsedInTasks();
            verify(categoryMapper).toResponse(category);
        }

        @Test
        @DisplayName("Should return empty list when no categories used in tasks")
        void shouldReturnEmptyListWhenNoCategoriesUsedInTasks() {
            // Given
            when(categoryRepository.findCategoriesUsedInTasks()).thenReturn(List.of());

            // When
            List<CategoryResponse> result = categoryService.getCategoriesUsedInTasks();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository).findCategoriesUsedInTasks();
        }
    }

    // Helper method
    private Category createCategory(Long id, String name, String description, Boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setActive(active);
        return category;
    }
}
