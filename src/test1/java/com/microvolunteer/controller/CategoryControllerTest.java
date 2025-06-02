package com.microvolunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.config.TestSecurityConfig;
import com.microvolunteer.controller.CategoryController.CreateCategoryRequest;
import com.microvolunteer.controller.CategoryController.UpdateCategoryRequest;
import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CategoryController Integration Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Test Category");
        categoryResponse.setDescription("Test Description");
        categoryResponse.setActive(true);
        categoryResponse.setTaskCount(5);
    }

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should get all active categories successfully")
        @WithMockUser
        void shouldGetAllActiveCategoriesSuccessfully() throws Exception {
            // Given
            List<CategoryResponse> categories = List.of(categoryResponse);
            when(categoryService.getAllActiveCategories()).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Category"))
                    .andExpect(jsonPath("$[0].description").value("Test Description"))
                    .andExpect(jsonPath("$[0].active").value(true));

            verify(categoryService).getAllActiveCategories();
        }

        @Test
        @DisplayName("Should return empty list when no categories")
        @WithMockUser
        void shouldReturnEmptyListWhenNoCategories() throws Exception {
            // Given
            when(categoryService.getAllActiveCategories()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(categoryService).getAllActiveCategories();
        }

        @Test
        @DisplayName("Should get category by ID successfully")
        @WithMockUser
        void shouldGetCategoryByIdSuccessfully() throws Exception {
            // Given
            when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

            // When & Then
            mockMvc.perform(get("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Category"))
                    .andExpect(jsonPath("$.description").value("Test Description"));

            verify(categoryService).getCategoryById(1L);
        }

        @Test
        @DisplayName("Should return 404 when category not found by ID")
        @WithMockUser
        void shouldReturn404WhenCategoryNotFoundById() throws Exception {
            // Given
            when(categoryService.getCategoryById(1L))
                    .thenThrow(new BusinessException("Category not found", "CATEGORY_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/categories/1"))
                    .andExpect(status().isNotFound());

            verify(categoryService).getCategoryById(1L);
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isUnauthorized());

            verify(categoryService, never()).getAllActiveCategories();
        }
    }

    @Nested
    @DisplayName("Search Categories Tests")
    class SearchCategoriesTests {

        @Test
        @DisplayName("Should search categories successfully")
        @WithMockUser
        void shouldSearchCategoriesSuccessfully() throws Exception {
            // Given
            List<CategoryResponse> categories = List.of(categoryResponse);
            when(categoryService.searchCategories("test")).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/api/categories/search")
                            .param("searchText", "test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Category"));

            verify(categoryService).searchCategories("test");
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        @WithMockUser
        void shouldReturnEmptyListWhenNoMatchesFound() throws Exception {
            // Given
            when(categoryService.searchCategories("nonexistent")).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/categories/search")
                            .param("searchText", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(categoryService).searchCategories("nonexistent");
        }

        @Test
        @DisplayName("Should return 400 when search text is missing")
        @WithMockUser
        void shouldReturn400WhenSearchTextMissing() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/categories/search"))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).searchCategories(anyString());
        }
    }

    @Nested
    @DisplayName("Categories with Task Count Tests")
    class CategoriesWithTaskCountTests {

        @Test
        @DisplayName("Should get categories with task count successfully")
        @WithMockUser
        void shouldGetCategoriesWithTaskCountSuccessfully() throws Exception {
            // Given
            List<CategoryResponse> categories = List.of(categoryResponse);
            when(categoryService.getCategoriesWithTaskCount()).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/api/categories/with-task-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].taskCount").value(5));

            verify(categoryService).getCategoriesWithTaskCount();
        }
    }

    @Nested
    @DisplayName("Used Categories Tests")
    class UsedCategoriesTests {

        @Test
        @DisplayName("Should get categories used in tasks successfully")
        @WithMockUser
        void shouldGetCategoriesUsedInTasksSuccessfully() throws Exception {
            // Given
            List<CategoryResponse> categories = List.of(categoryResponse);
            when(categoryService.getCategoriesUsedInTasks()).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/api/categories/used"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(categoryService).getCategoriesUsedInTasks();
        }
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully when user is admin")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateCategorySuccessfullyWhenUserIsAdmin() throws Exception {
            // Given
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("New Category");
            request.setDescription("New Description");

            when(categoryService.createCategory("New Category", "New Description"))
                    .thenReturn(categoryResponse);

            // When & Then
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Category"));

            verify(categoryService).createCategory("New Category", "New Description");
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn403WhenUserNotAdmin() throws Exception {
            // Given
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("New Category");
            request.setDescription("New Description");

            // When & Then
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(categoryService, never()).createCategory(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenRequestInvalid() throws Exception {
            // Given
            CreateCategoryRequest request = new CreateCategoryRequest();
            // Missing required name field

            // When & Then
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 409 when category name already exists")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn409WhenCategoryNameExists() throws Exception {
            // Given
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Existing Category");
            request.setDescription("Description");

            when(categoryService.createCategory("Existing Category", "Description"))
                    .thenThrow(new BusinessException("Category already exists", "CATEGORY_ALREADY_EXISTS"));

            // When & Then
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(categoryService).createCategory("Existing Category", "Description");
        }

        @Test
        @DisplayName("Should return 400 when name is too long")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenNameTooLong() throws Exception {
            // Given
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("A".repeat(101)); // Exceeds 100 character limit
            request.setDescription("Description");

            // When & Then
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when description is too long")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenDescriptionTooLong() throws Exception {
            // Given
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Valid Name");
            request.setDescription("A".repeat(501)); // Exceeds 500 character limit

            // When & Then
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully when user is admin")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateCategorySuccessfullyWhenUserIsAdmin() throws Exception {
            // Given
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setName("Updated Category");
            request.setDescription("Updated Description");

            when(categoryService.updateCategory(1L, "Updated Category", "Updated Description"))
                    .thenReturn(categoryResponse);

            // When & Then
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Category"));

            verify(categoryService).updateCategory(1L, "Updated Category", "Updated Description");
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn403WhenUserNotAdmin() throws Exception {
            // Given
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setName("Updated Category");
            request.setDescription("Updated Description");

            // When & Then
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(categoryService, never()).updateCategory(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            // Given
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setName("Updated Category");
            request.setDescription("Updated Description");

            when(categoryService.updateCategory(1L, "Updated Category", "Updated Description"))
                    .thenThrow(new BusinessException("Category not found", "CATEGORY_NOT_FOUND"));

            // When & Then
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(categoryService).updateCategory(1L, "Updated Category", "Updated Description");
        }

        @Test
        @DisplayName("Should return 409 when updated name already exists")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn409WhenUpdatedNameExists() throws Exception {
            // Given
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setName("Existing Name");
            request.setDescription("Description");

            when(categoryService.updateCategory(1L, "Existing Name", "Description"))
                    .thenThrow(new BusinessException("Category already exists", "CATEGORY_ALREADY_EXISTS"));

            // When & Then
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(categoryService).updateCategory(1L, "Existing Name", "Description");
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenRequestInvalid() throws Exception {
            // Given
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            // Missing required name field

            // When & Then
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).updateCategory(anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Deactivate Category Tests")
    class DeactivateCategoryTests {

        @Test
        @DisplayName("Should deactivate category successfully when user is admin")
        @WithMockUser(roles = "ADMIN")
        void shouldDeactivateCategorySuccessfullyWhenUserIsAdmin() throws Exception {
            // Given
            doNothing().when(categoryService).deactivateCategory(1L);

            // When & Then
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isNoContent());

            verify(categoryService).deactivateCategory(1L);
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn403WhenUserNotAdmin() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isForbidden());

            verify(categoryService, never()).deactivateCategory(anyLong());
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            // Given
            doThrow(new BusinessException("Category not found", "CATEGORY_NOT_FOUND"))
                    .when(categoryService).deactivateCategory(1L);

            // When & Then
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isNotFound());

            verify(categoryService).deactivateCategory(1L);
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isUnauthorized());

            verify(categoryService, never()).deactivateCategory(anyLong());
        }
    }

    @Nested
    @DisplayName("Path Variable Validation Tests")
    class PathVariableValidationTests {

        @Test
        @DisplayName("Should handle invalid category ID")
        @WithMockUser
        void shouldHandleInvalidCategoryId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/categories/invalid"))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).getCategoryById(anyLong());
        }

        @Test
        @DisplayName("Should handle negative category ID")
        @WithMockUser
        void shouldHandleNegativeCategoryId() throws Exception {
            // Given
            when(categoryService.getCategoryById(-1L))
                    .thenThrow(new BusinessException("Category not found", "CATEGORY_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/categories/-1"))
                    .andExpect(status().isNotFound());

            verify(categoryService).getCategoryById(-1L);
        }
    }
}
