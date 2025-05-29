package com.microvolunteer.service;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    private Category testCategory1;
    private Category testCategory2;
    private CategoryResponse testCategoryResponse1;
    private CategoryResponse testCategoryResponse2;

    @BeforeEach
    void setUp() {
        testCategory1 = Category.builder()
                .id(1L)
                .name("Допомога з покупками")
                .description("Допомога з покупками продуктів")
                .build();

        testCategory2 = Category.builder()
                .id(2L)
                .name("Транспорт")
                .description("Допомога з транспортуванням")
                .build();

        testCategoryResponse1 = CategoryResponse.builder()
                .id(1L)
                .name("Допомога з покупками")
                .description("Допомога з покупками продуктів")
                .build();

        testCategoryResponse2 = CategoryResponse.builder()
                .id(2L)
                .name("Транспорт")
                .description("Допомога з транспортуванням")
                .build();
    }

    @Test
    @DisplayName("Отримання всіх категорій - успішно")
    void getAllCategories_Success() {
        // Given
        List<Category> categories = Arrays.asList(testCategory1, testCategory2);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponse(testCategory1)).thenReturn(testCategoryResponse1);
        when(categoryMapper.toResponse(testCategory2)).thenReturn(testCategoryResponse2);

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Допомога з покупками");
        assertThat(result.get(1).getName()).isEqualTo("Транспорт");

        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponse(testCategory1);
        verify(categoryMapper).toResponse(testCategory2);
    }

    @Test
    @DisplayName("Отримання всіх категорій - порожній список")
    void getAllCategories_EmptyList() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(categoryRepository).findAll();
        verifyNoInteractions(categoryMapper);
    }
}
