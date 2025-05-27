package com.microvolunteer.service;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.TaskMapper;
import com.microvolunteer.repository.CategoryRepository;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRepository participationRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse createTask(String keycloakId, TaskCreateRequest request) {
        log.info("Створення нового завдання користувачем: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        // Перевірка типу користувача
        if (user.getUserType() != UserType.VULNERABLE) {
            throw BusinessException.forbidden("Тільки вразливі люди можуть створювати завдання");
        }

        // Перевірка категорії
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> BusinessException.notFound("Категорію не знайдено"));

        // Створення завдання
        Task task = taskMapper.toEntity(request);
        task.setCreator(user);
        task.setCategory(category);

        // Перевірка дати закінчення
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw BusinessException.badRequest("Дата закінчення не може бути раніше дати початку");
        }

        Task savedTask = taskRepository.save(task);
        log.info("Завдання {} успішно створено", savedTask.getId());

        TaskResponse response = taskMapper.toResponse(savedTask);
        response.setCanJoin(false);
        response.setIsParticipant(false);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(TaskSearchRequest request, String keycloakId) {
        log.info("Пошук завдань з параметрами: {}", request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        LocalDateTime dateFrom = request.getDateFrom() != null ?
                request.getDateFrom().atStartOfDay() : null;
        LocalDateTime dateTo = request.getDateTo() != null ?
                request.getDateTo().atTime(23, 59, 59) : null;

        Page<Task> tasks = taskRepository.searchTasks(
                request.getQuery(),
                request.getCategoryId(),
                request.getStatus(),
                dateFrom,
                dateTo,
                pageable
        );

        User currentUser = null;
        if (keycloakId != null) {
            currentUser = userRepository.findByKeycloakId(keycloakId).orElse(null);
        }

        final User user = currentUser;

        return tasks.map(task -> {
            TaskResponse response = taskMapper.toResponse(task);

            if (user != null) {
                boolean isParticipant = participationRepository
                        .existsByTaskIdAndUserId(task.getId(), user.getId());
                boolean canJoin = !isParticipant &&
                        task.getStatus() == TaskStatus.OPEN &&
                        task.getAvailableSpots() > 0 &&
                        !task.isPastDue() &&
                        user.getUserType() == UserType.VOLUNTEER &&
                        !task.getCreator().getId().equals(user.getId());

                response.setIsParticipant(isParticipant);
                response.setCanJoin(canJoin);
            } else {
                response.setIsParticipant(false);
                response.setCanJoin(false);
            }

            return response;
        });
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, String keycloakId) {
        log.info("Отримання завдання з ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.notFound("Завдання не знайдено"));

        TaskResponse response = taskMapper.toResponse(task);

        if (keycloakId != null) {
            User user = userRepository.findByKeycloakId(keycloakId).orElse(null);
            if (user != null) {
                boolean isParticipant = participationRepository
                        .existsByTaskIdAndUserId(task.getId(), user.getId());
                boolean canJoin = !isParticipant &&
                        task.getStatus() == TaskStatus.OPEN &&
                        task.getAvailableSpots() > 0 &&
                        !task.isPastDue() &&
                        user.getUserType() == UserType.VOLUNTEER &&
                        !task.getCreator().getId().equals(user.getId());

                response.setIsParticipant(isParticipant);
                response.setCanJoin(canJoin);
            }
        }

        return response;
    }

    @Transactional
    public TaskResponse updateTask(String keycloakId, Long taskId, TaskCreateRequest request) {
        log.info("Оновлення завдання {} користувачем: {}", taskId, keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.notFound("Завдання не знайдено"));

        // Перевірка прав
        if (!task.getCreator().getId().equals(user.getId())) {
            throw BusinessException.forbidden("У вас немає дозволу на редагування цього завдання");
        }

        // Перевірка категорії
        if (!task.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> BusinessException.notFound("Категорію не знайдено"));
            task.setCategory(category);
        }

        // Оновлення полів
        taskMapper.updateEntityFromRequest(request, task);

        Task updatedTask = taskRepository.save(task);
        log.info("Завдання {} успішно оновлено", updatedTask.getId());

        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    public TaskResponse joinTask(String keycloakId, Long taskId) {
        log.info("Приєднання до завдання {} користувачем: {}", taskId, keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.notFound("Завдання не знайдено"));

        // Перевірка типу користувача
        if (user.getUserType() != UserType.VOLUNTEER) {
            throw BusinessException.forbidden("Тільки волонтери можуть приєднуватися до завдань");
        }

        // Перевірка чи не автор
        if (task.getCreator().getId().equals(user.getId())) {
            throw BusinessException.badRequest("Ви не можете приєднатися до власного завдання");
        }

        // Перевірка статусу
        if (task.getStatus() != TaskStatus.OPEN) {
            throw BusinessException.badRequest("Це завдання не є відкритим для участі");
        }

        // Перевірка вільних місць
        if (task.getAvailableSpots() <= 0) {
            throw BusinessException.badRequest("Усі місця для цього завдання вже зайняті");
        }

        // Перевірка дати
        if (task.isPastDue()) {
            throw BusinessException.badRequest("Це завдання вже минуло");
        }

        // Перевірка чи вже учасник
        if (participationRepository.existsByTaskIdAndUserId(task.getId(), user.getId())) {
            throw BusinessException.badRequest("Ви вже приєдналися до цього завдання");
        }

        // Створення участі
        Participation participation = Participation.builder()
                .task(task)
                .user(user)
                .build();
        participationRepository.save(participation);

        // Оновлення статусу якщо всі місця заповнені
        if (task.getAvailableSpots() == 1) { // було останнє місце
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }

        log.info("Користувач {} успішно приєднався до завдання {}", user.getUsername(), task.getId());

        TaskResponse response = taskMapper.toResponse(task);
        response.setIsParticipant(true);
        response.setCanJoin(false);

        return response;
    }

    @Transactional
    public TaskResponse leaveTask(String keycloakId, Long taskId) {
        log.info("Відмова від участі у завданні {} користувачем: {}", taskId, keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.notFound("Завдання не знайдено"));

        Participation participation = participationRepository
                .findByTaskIdAndUserId(task.getId(), user.getId())
                .orElseThrow(() -> BusinessException.notFound("Ви не є учасником цього завдання"));

        // Перевірка дати
        if (task.isPastDue()) {
            throw BusinessException.badRequest("Ви не можете покинути завдання, яке вже розпочалося");
        }

        // Видалення участі
        participationRepository.delete(participation);

        // Оновлення статусу якщо було в процесі
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            task.setStatus(TaskStatus.OPEN);
            taskRepository.save(task);
        }

        log.info("Користувач {} успішно покинув завдання {}", user.getUsername(), task.getId());

        TaskResponse response = taskMapper.toResponse(task);
        response.setIsParticipant(false);
        response.setCanJoin(true);

        return response;
    }

    @Transactional
    public TaskResponse completeTask(String keycloakId, Long taskId) {
        log.info("Завершення завдання {} користувачем: {}", taskId, keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.notFound("Завдання не знайдено"));

        // Перевірка прав
        if (!task.getCreator().getId().equals(user.getId())) {
            throw BusinessException.forbidden("У вас немає дозволу на завершення цього завдання");
        }

        // Перевірка статусу
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw BusinessException.badRequest("Це завдання вже завершено");
        }

        if (task.getStatus() == TaskStatus.CANCELLED) {
            throw BusinessException.badRequest("Неможливо завершити скасоване завдання");
        }

        task.setStatus(TaskStatus.COMPLETED);
        Task updatedTask = taskRepository.save(task);

        log.info("Завдання {} успішно завершено", task.getId());

        return taskMapper.toResponse(updatedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getRecentTasks() {
        log.info("Отримання останніх завдань");

        List<Task> tasks = taskRepository.findTop6ByStatusOrderByCreatedAtDesc(TaskStatus.OPEN);

        return tasks.stream()
                .map(task -> {
                    TaskResponse response = taskMapper.toResponse(task);
                    response.setCanJoin(false);
                    response.setIsParticipant(false);
                    return response;
                })
                .collect(Collectors.toList());
    }
}