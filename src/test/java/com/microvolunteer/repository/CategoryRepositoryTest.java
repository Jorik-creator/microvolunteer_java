package com.microvolunteer.repository;

import com.microvolunteer.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByName_ExistingCategory_ShouldReturnCategory() {
        // Given
        Category category = createTestCategory("Test Category");
        categoryRepository.save(category);

        // When
        Optional<Category> found = categoryRepository.findByName("Test Category");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Category", found.get().getName());
    }

    @Test
    void findByName_NonExistingCategory_ShouldReturnEmpty() {
        // When
        Optional<Category> found = categoryRepository.findByName("Non-existent Category");

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveCategories() {
        // Given
        Category activeCategory = createTestCategory("Active Category");
        activeCategory.setIsActive(true);
        categoryRepository.save(activeCategory);

        Category inactiveCategory = createTestCategory("Inactive Category");
        inactiveCategory.setIsActive(false);
        categoryRepository.save(inactiveCategory);

        // When
        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();

        // Then
        // Note: There are 6 categories from migration + 1 test category = 7 total
        assertEquals(7, activeCategories.size());
        assertTrue(activeCategories.stream()
                .anyMatch(cat -> cat.getName().equals("Active Category")));
        assertFalse(activeCategories.stream()
                .anyMatch(cat -> cat.getName().equals("Inactive Category")));
    }

    @Test
    void findByIsActiveTrueOrderByNameAsc_ShouldReturnActiveCategoriesInAlphabeticalOrder() {
        // Given
        Category categoryB = createTestCategory("B Category");
        categoryB.setIsActive(true);
        categoryRepository.save(categoryB);

        Category categoryA = createTestCategory("A Category");
        categoryA.setIsActive(true);
        categoryRepository.save(categoryA);

        Category categoryC = createTestCategory("C Category");
        categoryC.setIsActive(false); // Inactive, should not be returned
        categoryRepository.save(categoryC);

        // When
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();

        // Then
        assertFalse(categories.isEmpty());
        // Check that our test categories are in correct order
        List<String> testCategoryNames = categories.stream()
                .map(Category::getName)
                .filter(name -> name.equals("A Category") || name.equals("B Category"))
                .toList();
        
        assertEquals(2, testCategoryNames.size());
        assertEquals("A Category", testCategoryNames.get(0));
        assertEquals("B Category", testCategoryNames.get(1));
    }

    @Test
    void existsByName_ExistingCategory_ShouldReturnTrue() {
        // Given
        Category category = createTestCategory("Existing Category");
        categoryRepository.save(category);

        // When
        boolean exists = categoryRepository.existsByName("Existing Category");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByName_NonExistingCategory_ShouldReturnFalse() {
        // When
        boolean exists = categoryRepository.existsByName("Non-existing Category");

        // Then
        assertFalse(exists);
    }

    @Test
    void countByIsActiveTrue_ShouldReturnCountOfActiveCategories() {
        // Given
        Category activeCategory1 = createTestCategory("Active 1");
        activeCategory1.setIsActive(true);
        categoryRepository.save(activeCategory1);

        Category activeCategory2 = createTestCategory("Active 2");
        activeCategory2.setIsActive(true);
        categoryRepository.save(activeCategory2);

        Category inactiveCategory = createTestCategory("Inactive");
        inactiveCategory.setIsActive(false);
        categoryRepository.save(inactiveCategory);

        // When
        long count = categoryRepository.countByIsActiveTrue();

        // Then
        // 6 from migration + 2 test active categories = 8
        assertEquals(8, count);
    }

    @Test
    void save_ShouldPersistCategory() {
        // Given
        Category category = createTestCategory("New Category");

        // When
        Category savedCategory = categoryRepository.save(category);

        // Then
        assertNotNull(savedCategory.getId());
        assertEquals("New Category", savedCategory.getName());
        assertEquals("Test description", savedCategory.getDescription());
        assertTrue(savedCategory.getIsActive());
    }

    @Test
    void deleteById_ShouldRemoveCategory() {
        // Given
        Category category = createTestCategory("Category to Delete");
        category = categoryRepository.save(category);
        Long categoryId = category.getId();

        // When
        categoryRepository.deleteById(categoryId);

        // Then
        Optional<Category> deleted = categoryRepository.findById(categoryId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // Given
        Category category1 = createTestCategory("Category 1");
        Category category2 = createTestCategory("Category 2");
        categoryRepository.save(category1);
        categoryRepository.save(category2);

        // When
        List<Category> allCategories = categoryRepository.findAll();

        // Then
        // 6 from migration + 2 test categories = 8
        assertEquals(8, allCategories.size());
    }

    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test description");
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }
}
