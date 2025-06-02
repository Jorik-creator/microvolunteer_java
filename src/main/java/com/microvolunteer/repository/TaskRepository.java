package com.microvolunteer.repository;

import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Task entity operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find tasks by author
     */
    List<Task> findByAuthor(User author);
    
    /**
     * Find tasks by status
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * Find tasks by status with pagination
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    /**
     * Find tasks by author and status
     */
    List<Task> findByAuthorAndStatus(User author, TaskStatus status);
    
    /**
     * Complex search query with multiple filters
     */
    @Query("""
        SELECT DISTINCT t FROM Task t 
        LEFT JOIN t.categories c
        WHERE (:status IS NULL OR t.status = :status)
        AND (:authorId IS NULL OR t.author.id = :authorId)
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND (:searchText IS NULL OR 
             LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR 
             LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')))
        AND (:fromDate IS NULL OR t.createdAt >= :fromDate)
        AND (:toDate IS NULL OR t.createdAt <= :toDate)
        ORDER BY t.createdAt DESC
        """)
    Page<Task> findTasksWithFilters(
        @Param("status") TaskStatus status,
        @Param("authorId") Long authorId,
        @Param("categoryId") Long categoryId,
        @Param("searchText") String searchText,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
    
    /**
     * Find tasks by category
     */
    @Query("SELECT t FROM Task t JOIN t.categories c WHERE c.id = :categoryId")
    List<Task> findByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);
    
    /**
     * Count tasks by author
     */
    long countByAuthor(User author);
    
    /**
     * Find recent tasks
     */
    @Query("SELECT t FROM Task t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Task> findRecentTasks(@Param("since") LocalDateTime since);
    
    /**
     * Find tasks with deadline approaching
     */
    @Query("""
        SELECT t FROM Task t 
        WHERE t.deadline IS NOT NULL 
        AND t.deadline <= :deadline 
        AND t.status != :completedStatus
        ORDER BY t.deadline ASC
        """)
    List<Task> findTasksWithDeadlineApproaching(
        @Param("deadline") LocalDateTime deadline,
        @Param("completedStatus") TaskStatus completedStatus
    );
}
