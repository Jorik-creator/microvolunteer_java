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

    // Виправлений запит для обчислення годин
    @Query(value = "SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (t.end_date - t.start_date)) / 3600), 0) " +
            "FROM participations p " +
            "JOIN tasks t ON p.task_id = t.id " +
            "WHERE p.user_id = :userId AND t.status = 'COMPLETED' AND t.end_date IS NOT NULL",
            nativeQuery = true)
    long calculateTotalHoursForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT p.task.category) FROM Participation p WHERE p.user.id = :userId")
    long countDistinctCategoriesByUserId(@Param("userId") Long userId);

    // Спрощений запит для місячної активності
    @Query(value = "SELECT TO_CHAR(p.joined_at, 'YYYY-MM') as month, COUNT(p.id)::bigint as count " +
            "FROM participations p " +
            "WHERE p.user_id = :userId AND p.joined_at >= :fromDate " +
            "GROUP BY TO_CHAR(p.joined_at, 'YYYY-MM') " +
            "ORDER BY month",
            nativeQuery = true)
    java.util.List<Object[]> getMonthlyActivityForUserRaw(@Param("userId") Long userId,
                                                          @Param("fromDate") LocalDateTime fromDate);

    // Метод-обгортка для повернення Map
    default Map<String, Long> getMonthlyActivityForUser(Long userId, LocalDateTime fromDate) {
        return getMonthlyActivityForUserRaw(userId, fromDate).stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Modifying
    @Query("UPDATE Participation p SET p.status = :status WHERE p.task.id = :taskId")
    void updateParticipationStatus(@Param("taskId") Long taskId, @Param("status") String status);
}