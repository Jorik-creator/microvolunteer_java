package com.microvolunteer.integration;

import com.microvolunteer.dto.request.CategoryCreateRequest;
import com.microvolunteer.entity.Category;
import com.microvolunteer.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CategoryManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void getAllCategories_PositiveScenario_ShouldReturnAllCategories() throws Exception {
        // Given - create test categories
        Category category1 = new Category();
        category1.setName("Екологія");
        category1.setDescription("Екологічні проекти");
        category1.setIsActive(true);
        category1.setCreatedAt(LocalDateTime.now());
        category1.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setName("Освіта");
        category2.setDescription("Освітні ініціативи");
        category2.setIsActive(true);
        category2.setCreatedAt(LocalDateTime.now());
        category2.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category2);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8)) // 6 from migration + 2 test categories
                .andExpect(jsonPath("$[?(@.name == 'Екологія')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Освіта')]").exists());
    }

    @Test
    void getCategoryById_PositiveScenario_ShouldReturnCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test description");
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        // When & Then
        mockMvc.perform(get("/api/categories/" + category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void getCategoryById_NegativeScenario_CategoryNotFound_ShouldReturnNotFound() throws Exception {
        // Given - non-existent category ID
        Long nonExistentId = 999L;

        // When & Then
        mockMvc.perform(get("/api/categories/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_PositiveScenario_AdminCreatesCategory() throws Exception {
        // Given
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Нова категорія");
        request.setDescription("Опис нової категорії");

        // When & Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Нова категорія"))
                .andExpect(jsonPath("$.description").value("Опис нової категорії"))
                .andExpect(jsonPath("$.isActive").value(true));

        // Verify category was created in database
        Optional<Category> createdCategory = categoryRepository.findByName("Нова категорія");
        assertTrue(createdCategory.isPresent());
        assertEquals("Опис нової категорії", createdCategory.get().getDescription());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_NegativeScenario_NonAdminUser_ShouldReturnForbidden() throws Exception {
        // Given
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Категорія від користувача");
        request.setDescription("Опис");

        // When & Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_NegativeScenario_DuplicateName_ShouldReturnBadRequest() throws Exception {
        // Given - create existing category
        Category existingCategory = new Category();
        existingCategory.setName("Існуюча категорія");
        existingCategory.setDescription("Опис");
        existingCategory.setIsActive(true);
        existingCategory.setCreatedAt(LocalDateTime.now());
        existingCategory.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(existingCategory);

        // When - try to create category with same name
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Існуюча категорія");
        request.setDescription("Інший опис");

        // Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_NegativeScenario_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - request with invalid data
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(""); // Empty name
        request.setDescription("Valid description");

        // When & Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_PositiveScenario_AdminUpdatesCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Стара назва");
        category.setDescription("Старий опис");
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        CategoryCreateRequest updateRequest = new CategoryCreateRequest();
        updateRequest.setName("Нова назва");
        updateRequest.setDescription("Новий опис");

        // When & Then
        mockMvc.perform(put("/api/categories/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Нова назва"))
                .andExpect(jsonPath("$.description").value("Новий опис"));

        // Verify category was updated in database
        Optional<Category> updatedCategory = categoryRepository.findById(category.getId());
        assertTrue(updatedCategory.isPresent());
        assertEquals("Нова назва", updatedCategory.get().getName());
        assertEquals("Новий опис", updatedCategory.get().getDescription());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_NegativeScenario_CategoryNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        CategoryCreateRequest updateRequest = new CategoryCreateRequest();
        updateRequest.setName("Нова назва");
        updateRequest.setDescription("Новий опис");

        // When & Then
        mockMvc.perform(put("/api/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_PositiveScenario_AdminDeletesCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Категорія для видалення");
        category.setDescription("Опис категорії");
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        // When & Then
        mockMvc.perform(delete("/api/categories/" + category.getId()))
                .andExpect(status().isNoContent());

        // Verify category was deleted from database
        Optional<Category> deletedCategory = categoryRepository.findById(category.getId());
        assertFalse(deletedCategory.isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_NegativeScenario_CategoryNotFound_ShouldReturnNotFound() throws Exception {
        // Given - non-existent category ID
        Long nonExistentId = 999L;

        // When & Then
        mockMvc.perform(delete("/api/categories/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCategory_NegativeScenario_NonAdminUser_ShouldReturnForbidden() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Категорія");
        category.setDescription("Опис");
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        // When & Then
        mockMvc.perform(delete("/api/categories/" + category.getId()))
                .andExpect(status().isForbidden());
    }
}
