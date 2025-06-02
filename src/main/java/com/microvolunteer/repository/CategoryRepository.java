package com.microvolunteer.repository;

import com.microvolunteer.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);
    
    /**
     * Find all active categories
     */
    List<Category> findByActiveTrueOrderByNameAsc();
    
    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Find categories by name containing search text
     */
    @Query("""
        SELECT c FROM Category c 
        WHERE c.active = true 
        AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
        ORDER BY c.name ASC
        """)
    List<Category> findByNameContainingIgnoreCaseAndActiveTrue(@Param("searchText") String searchText);
    
    /**
     * Find categories with task count
     */
    @Query("""
        SELECT c, COUNT(t) as taskCount FROM Category c 
        LEFT JOIN c.tasks t 
        WHERE c.active = true 
        GROUP BY c.id 
        ORDER BY taskCount DESC, c.name ASC
        """)
    List<Object[]> findCategoriesWithTaskCount();
    
    /**
     * Find categories used in tasks
     */
    @Query("""
        SELECT DISTINCT c FROM Category c 
        JOIN c.tasks t 
        WHERE c.active = true 
        ORDER BY c.name ASC
        """)
    List<Category> findCategoriesUsedInTasks();
}
