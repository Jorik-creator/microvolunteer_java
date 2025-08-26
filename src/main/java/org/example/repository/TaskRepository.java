package org.example.repository;

import org.example.model.Task;
import org.example.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @EntityGraph(attributePaths = {"creator", "category"})
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"creator", "category"})
    Page<Task> findByCreatorId(Long creatorId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"creator", "category"})
    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"creator", "category", "participants"})
    @Override
    Page<Task> findAll(Pageable pageable);
    
    @Query("SELECT t FROM Task t JOIN t.participants p WHERE p.id = :userId")
    Page<Task> findTasksByParticipantId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE " +
           "(:title IS NULL OR t.title LIKE CONCAT('%', :title, '%')) AND " +
           "(:location IS NULL OR t.location LIKE CONCAT('%', :location, '%')) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:startDateFrom IS NULL OR t.startDate >= :startDateFrom) AND " +
           "(:startDateTo IS NULL OR t.startDate <= :startDateTo)")
    Page<Task> findTasksWithFilters(@Param("title") String title,
                                   @Param("location") String location,
                                   @Param("categoryId") Long categoryId,
                                   @Param("status") TaskStatus status,
                                   @Param("startDateFrom") LocalDateTime startDateFrom,
                                   @Param("startDateTo") LocalDateTime startDateTo,
                                   Pageable pageable);
    
    @Query("SELECT COUNT(t.participants) FROM Task t WHERE t.id = :taskId")
    Integer countParticipantsByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN true ELSE false END " +
           "FROM Task t JOIN t.participants tp WHERE t.id = :taskId AND tp.id = :userId")
    boolean isUserParticipant(@Param("taskId") Long taskId, @Param("userId") Long userId);
    
    List<Task> findByStartDateBeforeAndStatus(LocalDateTime dateTime, TaskStatus status);
}