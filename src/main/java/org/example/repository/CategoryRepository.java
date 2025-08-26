package org.example.repository;

import org.example.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.category.id = :categoryId")
    Long countTasksByCategoryId(@Param("categoryId") Long categoryId);
}