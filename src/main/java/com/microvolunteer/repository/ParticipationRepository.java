package com.microvolunteer.repository;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);
    
    boolean existsByUserAndTask(User user, Task task);

    Optional<Participation> findByTaskIdAndUserId(Long taskId, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.user.id = :userId AND p.task.status = :status")
    long countByUserIdAndTaskStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT COALESCE(SUM(EXTRACT(HOUR FROM (p.task.endDate - p.task.startDate))), 0) " +
            "FROM Participation p " +
            "WHERE p.user.id = :userId AND p.task.status = 'COMPLETED' AND p.task.endDate IS NOT NULL")
    long calculateTotalHoursForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT p.task.category) FROM Participation p WHERE p.user.id = :userId")
    long countDistinctCategoriesByUserId(@Param("userId") Long userId);

    @Query("SELECT TO_CHAR(p.joinedAt, 'YYYY-MM') as month, COUNT(p) as count " +
            "FROM Participation p " +
            "WHERE p.user.id = :userId AND p.joinedAt >= :fromDate " +
            "GROUP BY TO_CHAR(p.joinedAt, 'YYYY-MM') " +
            "ORDER BY month")
    Map<String, Long> getMonthlyActivityForUser(@Param("userId") Long userId,
                                                @Param("fromDate") LocalDateTime fromDate);
    
    @Modifying
    @Query("UPDATE Participation p SET p.status = :status WHERE p.task.id = :taskId")
    void updateParticipationStatus(@Param("taskId") Long taskId, @Param("status") String status);
}