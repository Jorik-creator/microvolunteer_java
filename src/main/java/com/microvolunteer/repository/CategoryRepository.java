package com.microvolunteer.repository;

import com.microvolunteer.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
    
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.category.id = :categoryId")
    boolean hasTasksInCategory(@Param("categoryId") Long categoryId);
}