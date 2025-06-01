package com.microvolunteer.repository;

import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    List<User> findByIsActiveTrue();

    // Виправлено firstName та lastName
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByName(@Param("name") String name);

    // Додано метод для пошуку за типом користувача
    List<User> findByUserType(UserType userType);

    boolean existsByEmail(String email);
}
