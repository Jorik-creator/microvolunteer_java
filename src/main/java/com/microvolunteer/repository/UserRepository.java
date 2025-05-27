package com.microvolunteer.repository;

import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByUserType(UserType userType);

    @Query("SELECT COUNT(DISTINCT p.user) FROM Participation p WHERE p.task.creator.id = :userId")
    long countVolunteersHelpedByUserId(Long userId);
}