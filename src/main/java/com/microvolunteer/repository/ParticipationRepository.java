package com.microvolunteer.repository;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    Optional<Participation> findByTaskIdAndUserId(Long taskId, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.user.id = :userId AND p.task.status = :status")
    long countByUserIdAndTaskStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(HOUR, p.task.startDate, p.task.endDate)), 0) " +
            "FROM Participation p " +
            "WHERE p.user.id = :userId AND p.task.status = 'COMPLETED' AND p.task.endDate IS NOT NULL")
    long calculateTotalHoursForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT p.task.category) FROM Participation p WHERE p.user.id = :userId")
    long countDistinctCategoriesByUserId(@Param("userId") Long userId);

    @Query("SELECT FUNCTION('DATE_FORMAT', p.joinedAt, '%Y-%m') as month, COUNT(p) as count " +
            "FROM Participation p " +
            "WHERE p.user.id = :userId AND p.joinedAt >= :fromDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.joinedAt, '%Y-%m') " +
            "ORDER BY month")
    Map<String, Long> getMonthlyActivityForUser(@Param("userId") Long userId,
                                                @Param("fromDate") LocalDateTime fromDate);
}