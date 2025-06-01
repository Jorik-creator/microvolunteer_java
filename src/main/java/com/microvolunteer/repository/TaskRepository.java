package com.microvolunteer.repository;

import com.microvolunteer.entity.Task;
import com.microvolunteer.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    List<Task> findByCategoryId(Long categoryId);

    List<Task> findByCreatorId(Long creatorId);

    // Додано для тестів
    List<Task> findByTitle(String title);

    @Query("SELECT t FROM Task t WHERE t.scheduledAt BETWEEN :start AND :end")
    List<Task> findTasksInDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.title LIKE %:keyword% OR t.description LIKE %:keyword%")
    List<Task> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.task.id = :taskId")
    int countParticipantsByTaskId(@Param("taskId") Long taskId);

    // Додано search метод для розв'язання помилки
    @Query("SELECT t FROM Task t WHERE " +
           "(:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:dateFrom IS NULL OR t.scheduledAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR t.scheduledAt <= :dateTo)")
    Page<Task> searchTasks(@Param("query") String query,
                          @Param("categoryId") Long categoryId,
                          @Param("status") TaskStatus status,
                          @Param("dateFrom") LocalDateTime dateFrom,
                          @Param("dateTo") LocalDateTime dateTo,
                          Pageable pageable);
}
