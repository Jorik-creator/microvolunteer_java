package com.microvolunteer.controller;

import com.microvolunteer.dto.request.CategoryCreateRequest;
import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_ShouldReturnCategoryList() throws Exception {
        // Given
        CategoryResponse category1 = new CategoryResponse();
        category1.setId(1L);
        category1.setName("Екологія");
        
        CategoryResponse category2 = new CategoryResponse();
        category2.setId(2L);
        category2.setName("Освіта");

        List<CategoryResponse> categories = Arrays.asList(category1, category2);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Екологія"))
                .andExpect(jsonPath("$[1].name").value("Освіта"));
    }

    @Test
    void getCategoryById_ExistingCategory_ShouldReturnCategory() throws Exception {
        // Given
        CategoryResponse category = new CategoryResponse();
        category.setId(1L);
        category.setName("Екологія");
        category.setDescription("Екологічні проекти");

        when(categoryService.getCategoryById(1L)).thenReturn(category);

        // When & Then
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Екологія"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_ValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Нова категорія");
        request.setDescription("Опис нової категорії");

        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Нова категорія");
        response.setDescription("Опис нової категорії");

        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Нова категорія"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_ValidRequest_ShouldReturnUpdatedCategory() throws Exception {
        // Given
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Оновлена категорія");
        request.setDescription("Оновлений опис");

        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Оновлена категорія");
        response.setDescription("Оновлений опис");

        when(categoryService.updateCategory(anyLong(), any(CategoryCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Оновлена категорія"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_ExistingCategory_ShouldReturnNoContent() throws Exception {
        // Given
        when(categoryService.deleteCategory(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_NonExistingCategory_ShouldReturnNotFound() throws Exception {
        // Given
        when(categoryService.deleteCategory(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/categories/999"))
                .andExpect(status().isNotFound());
    }
}
