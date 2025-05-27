package com.microvolunteer.service;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ParticipationRepository participationRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Отримання користувача з ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByKeycloakId(String keycloakId) {
        log.info("Отримання користувача з Keycloak ID: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        log.info("Отримання статистики для користувача: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userId", user.getId());
        statistics.put("username", user.getUsername());
        statistics.put("userType", user.getUserType());
        statistics.put("dateJoined", user.getDateJoined());
        statistics.put("daysAsMember",
                java.time.Duration.between(user.getDateJoined(), LocalDateTime.now()).toDays());

        if (user.getUserType() == UserType.VOLUNTEER) {
            // Статистика для волонтера
            long totalParticipations = participationRepository.countByUserId(userId);
            long completedTasks = participationRepository.countByUserIdAndTaskStatus(userId, TaskStatus.COMPLETED);
            long hoursHelped = participationRepository.calculateTotalHoursForUser(userId);
            long categoriesHelped = participationRepository.countDistinctCategoriesByUserId(userId);

            statistics.put("totalParticipations", totalParticipations);
            statistics.put("completedTasks", completedTasks);
            statistics.put("totalHoursHelped", hoursHelped);
            statistics.put("categoriesHelped", categoriesHelped);

            // Активність за останні 6 місяців
            Map<String, Long> monthlyActivity = participationRepository
                    .getMonthlyActivityForUser(userId, LocalDateTime.now().minusMonths(6));
            statistics.put("monthlyActivity", monthlyActivity);

        } else {
            // Статистика для вразливої людини
            long totalCreatedTasks = taskRepository.countByCreatorId(userId);
            long completedTasks = taskRepository.countByCreatorIdAndStatus(userId, TaskStatus.COMPLETED);
            long cancelledTasks = taskRepository.countByCreatorIdAndStatus(userId, TaskStatus.CANCELLED);
            long totalVolunteersHelped = userRepository.countVolunteersHelpedByUserId(userId);

            statistics.put("totalCreatedTasks", totalCreatedTasks);
            statistics.put("completedTasks", completedTasks);
            statistics.put("cancelledTasks", cancelledTasks);
            statistics.put("totalVolunteersHelped", totalVolunteersHelped);

            // Статистика за категоріями
            Map<String, Long> tasksByCategory = taskRepository.countTasksByCategoryForUser(userId);
            statistics.put("tasksByCategory", tasksByCategory);
        }

        return statistics;
    }

    @Transactional
    public UserResponse updateProfile(String keycloakId, Map<String, String> updates) {
        log.info("Оновлення профілю для користувача: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        // Оновлення дозволених полів
        if (updates.containsKey("firstName")) {
            user.setFirstName(updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName(updates.get("lastName"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("bio")) {
            user.setBio(updates.get("bio"));
        }
        if (updates.containsKey("address")) {
            user.setAddress(updates.get("address"));
        }
        if (updates.containsKey("profileImage")) {
            user.setProfileImage(updates.get("profileImage"));
        }

        User updatedUser = userRepository.save(user);
        log.info("Профіль користувача {} успішно оновлено", user.getUsername());

        return userMapper.toResponse(updatedUser);
    }
}