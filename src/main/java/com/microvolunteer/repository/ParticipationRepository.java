package com.microvolunteer.repository;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Participation entity operations.
 */
@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    
    /**
     * Find participation by volunteer and task
     */
    Optional<Participation> findByVolunteerAndTask(User volunteer, Task task);
    
    /**
     * Find active participation by volunteer and task
     */
    Optional<Participation> findByVolunteerAndTaskAndActiveTrue(User volunteer, Task task);
    
    /**
     * Find all participations by volunteer
     */
    List<Participation> findByVolunteer(User volunteer);
    
    /**
     * Find active participations by volunteer
     */
    List<Participation> findByVolunteerAndActiveTrueOrderByJoinedAtDesc(User volunteer);
    
    /**
     * Find all participations by task
     */
    List<Participation> findByTask(Task task);
    
    /**
     * Find active participations by task
     */
    List<Participation> findByTaskAndActiveTrueOrderByJoinedAtAsc(Task task);
    
    /**
     * Check if volunteer already participates in task
     */
    boolean existsByVolunteerAndTaskAndActiveTrue(User volunteer, Task task);
    
    /**
     * Count active participations by volunteer
     */
    long countByVolunteerAndActiveTrue(User volunteer);
    
    /**
     * Count active participations by task
     */
    long countByTaskAndActiveTrue(Task task);
    
    /**
     * Find volunteer statistics
     */
    @Query("""
        SELECT p.volunteer, COUNT(p) as participationCount 
        FROM Participation p 
        WHERE p.active = true 
        GROUP BY p.volunteer 
        ORDER BY participationCount DESC
        """)
    List<Object[]> findVolunteerStatistics();
    
    /**
     * Find participations with task details for a volunteer
     */
    @Query("""
        SELECT p FROM Participation p 
        JOIN FETCH p.task t 
        JOIN FETCH t.author 
        WHERE p.volunteer = :volunteer 
        AND p.active = true 
        ORDER BY p.joinedAt DESC
        """)
    List<Participation> findActiveParticipationsWithTaskDetails(@Param("volunteer") User volunteer);
}
