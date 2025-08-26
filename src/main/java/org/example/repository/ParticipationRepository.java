package org.example.repository;

import org.example.model.Participation;
import org.example.model.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    
    Optional<Participation> findByTaskIdAndUserId(Long taskId, Long userId);
    
    List<Participation> findByTaskIdAndStatus(Long taskId, ParticipationStatus status);
    
    List<Participation> findByUserIdAndStatus(Long userId, ParticipationStatus status);
    
    @Query("SELECT COUNT(p) FROM Participation p WHERE p.task.id = :taskId AND p.status = 'ACTIVE'")
    Long countActiveParticipantsByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT COUNT(p) FROM Participation p WHERE p.user.id = :userId AND p.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ParticipationStatus status);
}