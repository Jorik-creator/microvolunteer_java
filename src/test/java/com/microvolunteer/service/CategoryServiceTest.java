package com.microvolunteer.service;

import com.microvolunteer.dto.request.CategoryCreateRequest;
import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.exception.ResourceNotFoundException;
import com.microvolunteer.exception.ValidationException;
import com.microvolunteer.mapper.CategoryMapper;
import com.microvolunteer.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private Category testCategory2;
    private CategoryResponse testCategoryResponse;
    private CategoryResponse testCategoryResponse2;
    private CategoryCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Освіта")
                .description("Допомога в навчанні та освітні проекти")
                .icon("education")
                .color("#4CAF50")
                .build();

        testCategory2 = Category.builder()
                .id(2L)
                .name("Екологія")
                .description("Екологічні проекти та збереження довкілля")
                .icon("ecology")
                .color("#2196F3")
                .build();

        testCategoryResponse = new CategoryResponse();
        testCategoryResponse.setId(1L);
        testCategoryResponse.setName("Освіта");
        testCategoryResponse.setDescription("Допомога в навчанні та освітні проекти");
        testCategoryResponse.setIcon("education");
        testCategoryResponse.setColor("#4CAF50");

        testCategoryResponse2 = new CategoryResponse();
        testCategoryResponse2.setId(2L);
        testCategoryResponse2.setName("Екологія");
        testCategoryResponse2.setDescription("Екологічні проекти та збереження довкілля");
        testCategoryResponse2.setIcon("ecology");
        testCategoryResponse2.setColor("#2196F3");

        createRequest = new CategoryCreateRequest();
        createRequest.setName("Соціальна допомога");
        createRequest.setDescription("Допомога людям у скрутних життєвих обставинах");
        createRequest.setIcon("social");
        createRequest.setColor("#FF9800");
    }

    @Test
    @DisplayName("Отримання всіх категорій - успішно")
    void getAllCategories_Success() {
        // Given
        List<Category> categories = Arrays.asList(testCategory, testCategory2);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);
        when(categoryMapper.toResponse(testCategory2)).thenReturn(testCategoryResponse2);

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(testCategoryResponse.getId());
        assertThat(result.get(1).getId()).isEqualTo(testCategoryResponse2.getId());

        verify(categoryRepository).findAll();
        verify(categoryMapper, times(2)).toResponse(any(Category.class));
    }

    @Test
    @DisplayName("Отримання категорії за ID - успішно")
    void getCategoryById_Success() {
        // Given
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.getCategoryById(testCategory.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCategoryResponse.getId());
        assertThat(result.getName()).isEqualTo(testCategoryResponse.getName());

        verify(categoryRepository).findById(testCategory.getId());
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    @DisplayName("Отримання категорії за ID - не знайдено")
    void getCategoryById_NotFound() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категорію не знайдено");

        verify(categoryRepository).findById(999L);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Створення категорії - успішно")
    void createCategory_Success() {
        // Given
        when(categoryRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.createCategory(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testCategoryResponse.getName());

        verify(categoryRepository).existsByName(createRequest.getName());
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    @DisplayName("Створення категорії - назва вже існує")
    void createCategory_NameExists() {
        // Given
        when(categoryRepository.existsByName(createRequest.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Категорія з такою назвою вже існує");

        verify(categoryRepository).existsByName(createRequest.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Оновлення категорії - успішно")
    void updateCategory_Success() {
        // Given
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndIdNot(createRequest.getName(), testCategory.getId())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.updateCategory(testCategory.getId(), createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testCategory.getName()).isEqualTo(createRequest.getName());
        assertThat(testCategory.getDescription()).isEqualTo(createRequest.getDescription());
        assertThat(testCategory.getIcon()).isEqualTo(createRequest.getIcon());
        assertThat(testCategory.getColor()).isEqualTo(createRequest.getColor());

        verify(categoryRepository).findById(testCategory.getId());
        verify(categoryRepository).existsByNameAndIdNot(createRequest.getName(), testCategory.getId());
        verify(categoryRepository).save(testCategory);
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    @DisplayName("Оновлення категорії - не знайдено")
    void updateCategory_NotFound() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(999L, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категорію не знайдено");

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Оновлення категорії - назва вже існує")
    void updateCategory_NameExists() {
        // Given
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndIdNot(createRequest.getName(), testCategory.getId())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(testCategory.getId(), createRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Категорія з такою назвою вже існує");

        verify(categoryRepository).findById(testCategory.getId());
        verify(categoryRepository).existsByNameAndIdNot(createRequest.getName(), testCategory.getId());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Видалення категорії - успішно")
    void deleteCategory_Success() {
        // Given
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasTasksInCategory(testCategory.getId())).thenReturn(false);

        // When
        categoryService.deleteCategory(testCategory.getId());

        // Then
        verify(categoryRepository).findById(testCategory.getId());
        verify(categoryRepository).hasTasksInCategory(testCategory.getId());
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("Видалення категорії - не знайдено")
    void deleteCategory_NotFound() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категорію не знайдено");

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Видалення категорії - має завдання")
    void deleteCategory_HasTasks() {
        // Given
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasTasksInCategory(testCategory.getId())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(testCategory.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Не можна видалити категорію, яка має завдання");

        verify(categoryRepository).findById(testCategory.getId());
        verify(categoryRepository).hasTasksInCategory(testCategory.getId());
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Отримання категорії за назвою - успішно")
    void getCategoryByName_Success() {
        // Given
        when(categoryRepository.findByName(testCategory.getName())).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.getCategoryByName(testCategory.getName());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testCategoryResponse.getName());

        verify(categoryRepository).findByName(testCategory.getName());
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    @DisplayName("Отримання категорії за назвою - не знайдено")
    void getCategoryByName_NotFound() {
        // Given
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryByName("Неіснуюча категорія"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категорію не знайдено");

        verify(categoryRepository).findByName("Неіснуюча категорія");
        verifyNoInteractions(categoryMapper);
    }
}
