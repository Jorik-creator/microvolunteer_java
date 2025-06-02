package com.microvolunteer.repository;

import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Keycloak subject ID
     */
    Optional<User> findByKeycloakSubject(String keycloakSubject);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find users by type
     */
    List<User> findByUserType(UserType userType);
    
    /**
     * Find active users by type
     */
    List<User> findByUserTypeAndActiveTrue(UserType userType);
    
    /**
     * Check if user exists by Keycloak subject
     */
    boolean existsByKeycloakSubject(String keycloakSubject);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find volunteers with statistics
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.userType = :userType AND u.active = true
        ORDER BY u.createdAt DESC
        """)
    List<User> findActiveUsersByType(@Param("userType") UserType userType);
}
