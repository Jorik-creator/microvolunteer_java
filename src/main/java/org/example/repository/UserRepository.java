package org.example.repository;

import org.example.model.TaskStatus;
import org.example.model.User;
import org.example.model.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Page<User> findByUserType(UserType userType, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.creator.id = :userId AND t.status = :status")
    Long countTasksByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t JOIN t.participants p WHERE p.id = :userId AND t.status = :status")
    Long countCompletedParticipationsByUserId(@Param("userId") Long userId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(DISTINCT p) FROM Task t JOIN t.participants p WHERE t.creator.id = :userId")
    Long countVolunteersHelpedByUserId(@Param("userId") Long userId);
}