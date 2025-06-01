package com.microvolunteer.repository;

import com.microvolunteer.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    List<Participation> findByUserId(Long userId);
    
    List<Participation> findByUserIdOrderByJoinedAtDesc(Long userId);

    List<Participation> findByTaskId(Long taskId);

    Optional<Participation> findByUserIdAndTaskId(Long userId, Long taskId);
    
    // Додано для консистентності з ParticipationService
    Optional<Participation> findByTaskIdAndUserId(Long taskId, Long userId);

    boolean existsByUserIdAndTaskId(Long userId, Long taskId);
    
    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    // Додано метод для підрахунку учасників
    long countByTaskId(Long taskId);
    
    long countByUserId(Long userId);
}
