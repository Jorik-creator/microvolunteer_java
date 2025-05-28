package com.microvolunteer.repository;

import com.microvolunteer.entity.Task;
import com.microvolunteer.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByCreatorId(Long creatorId, Pageable pageable);

    List<Task> findTop6ByStatusOrderByCreatedAtDesc(TaskStatus status);

    long countByStatus(TaskStatus status);

    long countByCreatorId(Long creatorId);

    long countByCreatorIdAndStatus(Long creatorId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE " +
            "(:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.location) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:dateFrom IS NULL OR t.startDate >= :dateFrom) AND " +
            "(:dateTo IS NULL OR t.startDate <= :dateTo)")
    Page<Task> searchTasks(@Param("query") String query,
                           @Param("categoryId") Long categoryId,
                           @Param("status") TaskStatus status,
                           @Param("dateFrom") LocalDateTime dateFrom,
                           @Param("dateTo") LocalDateTime dateTo,
                           Pageable pageable);

    @Query("SELECT c.name as category, COUNT(t) as count " +
            "FROM Task t JOIN t.category c " +
            "WHERE t.creator.id = :userId " +
            "GROUP BY c.name")
    List<Object[]> findTasksByCategoryForUser(@Param("userId") Long userId);
}