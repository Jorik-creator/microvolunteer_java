package com.microvolunteer.service;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.TaskMapper;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for task operations.
 */
@Service
@Transactional
public class TaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    
    private final TaskRepository taskRepository;
    private final ParticipationRepository participationRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final CategoryService categoryService;
    
    public TaskService(TaskRepository taskRepository,
                      ParticipationRepository participationRepository,
                      TaskMapper taskMapper,
                      UserService userService,
                      CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.participationRepository = participationRepository;
        this.taskMapper = taskMapper;
        this.userService = userService;
        this.categoryService = categoryService;
    }
    
    /**
     * Create a new task
     */
    public TaskResponse createTask(TaskCreateRequest request, String keycloakSubject) {
        logger.info("Creating new task with title: {}", request.getTitle());
        
        User author = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        // Only SENSITIVE users can create tasks
        if (author.getUserType() != UserType.SENSITIVE) {
            throw BusinessException.unauthorizedAccess("create task");
        }
        
        Task task = taskMapper.toEntity(request);
        task.setAuthor(author);
        
        // Set categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = categoryService.getCategoriesByIds(request.getCategoryIds());
            task.setCategories(categories);
        }
        
        Task savedTask = taskRepository.save(task);
        logger.info("Successfully created task with id: {}", savedTask.getId());
        
        return mapToResponseWithParticipantsCount(savedTask);
    }
    
    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> BusinessException.taskNotFound(id));
        
        return mapToResponseWithParticipantsCount(task);
    }
    
    /**
     * Get task entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public Task getTaskEntityById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> BusinessException.taskNotFound(id));
    }
    
    /**
     * Search tasks with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(TaskSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest);
        
        Page<Task> tasks = taskRepository.findTasksWithFilters(
            searchRequest.getStatus(),
            searchRequest.getAuthorId(),
            searchRequest.getCategoryId(),
            searchRequest.getSearchText(),
            searchRequest.getFromDate(),
            searchRequest.getToDate(),
            pageable
        );
        
        return tasks.map(this::mapToResponseWithParticipantsCount);
    }
    
    /**
     * Get tasks by author
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAuthor(String keycloakSubject) {
        User author = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        List<Task> tasks = taskRepository.findByAuthor(author);
        
        return tasks.stream()
            .map(this::mapToResponseWithParticipantsCount)
            .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by status
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        
        return tasks.stream()
            .map(this::mapToResponseWithParticipantsCount)
            .collect(Collectors.toList());
    }
    
    /**
     * Complete a task
     */
    public TaskResponse completeTask(Long taskId, String keycloakSubject) {
        logger.info("Completing task with id: {}", taskId);
        
        Task task = getTaskEntityById(taskId);
        User user = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        // Only task author can complete the task
        if (!task.getAuthor().getId().equals(user.getId())) {
            throw BusinessException.unauthorizedAccess("complete task");
        }
        
        // Task must be open or in progress to be completed
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw BusinessException.invalidTaskStatus(
                task.getStatus().toString(), 
                TaskStatus.COMPLETED.toString()
            );
        }
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        
        Task savedTask = taskRepository.save(task);
        logger.info("Successfully completed task with id: {}", savedTask.getId());
        
        return mapToResponseWithParticipantsCount(savedTask);
    }
    
    /**
     * Update task status to IN_PROGRESS when someone joins
     */
    public void updateTaskStatusToInProgress(Long taskId) {
        Task task = getTaskEntityById(taskId);
        
        if (task.getStatus() == TaskStatus.OPEN) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }
    }
    
    /**
     * Get recent tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getRecentTasks(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Task> tasks = taskRepository.findRecentTasks(since);
        
        return tasks.stream()
            .map(this::mapToResponseWithParticipantsCount)
            .collect(Collectors.toList());
    }
    
    /**
     * Get tasks with approaching deadline
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksWithApproachingDeadline(int hours) {
        LocalDateTime deadline = LocalDateTime.now().plusHours(hours);
        List<Task> tasks = taskRepository.findTasksWithDeadlineApproaching(deadline, TaskStatus.COMPLETED);
        
        return tasks.stream()
            .map(this::mapToResponseWithParticipantsCount)
            .collect(Collectors.toList());
    }
    
    /**
     * Get task statistics
     */
    @Transactional(readOnly = true)
    public TaskStatistics getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long openTasks = taskRepository.countByStatus(TaskStatus.OPEN);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        
        return new TaskStatistics(totalTasks, openTasks, inProgressTasks, completedTasks);
    }
    
    /**
     * Delete task (only by author and only if no active participations)
     */
    public void deleteTask(Long taskId, String keycloakSubject) {
        logger.info("Deleting task with id: {}", taskId);
        
        Task task = getTaskEntityById(taskId);
        User user = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        // Only task author or admin can delete the task
        if (!task.getAuthor().getId().equals(user.getId()) && user.getUserType() != UserType.ADMIN) {
            throw BusinessException.unauthorizedAccess("delete task");
        }
        
        // Check if there are active participations
        long activeParticipations = participationRepository.countByTaskAndActiveTrue(task);
        if (activeParticipations > 0) {
            throw new BusinessException("Cannot delete task with active participations", "TASK_HAS_PARTICIPANTS");
        }
        
        taskRepository.delete(task);
        logger.info("Successfully deleted task with id: {}", taskId);
    }
    
    /**
     * Helper method to create Pageable object
     */
    private Pageable createPageable(TaskSearchRequest searchRequest) {
        Sort.Direction direction = searchRequest.getSortDirection().equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, searchRequest.getSortBy());
        
        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }
    
    /**
     * Helper method to map Task to TaskResponse with participants count
     */
    private TaskResponse mapToResponseWithParticipantsCount(Task task) {
        TaskResponse response = taskMapper.toResponse(task);
        
        // Set participants count
        long participantsCount = participationRepository.countByTaskAndActiveTrue(task);
        response.setParticipantsCount((int) participantsCount);
        
        return response;
    }
    
    /**
     * Inner class for task statistics
     */
    public static class TaskStatistics {
        private final long total;
        private final long open;
        private final long inProgress;
        private final long completed;
        
        public TaskStatistics(long total, long open, long inProgress, long completed) {
            this.total = total;
            this.open = open;
            this.inProgress = inProgress;
            this.completed = completed;
        }
        
        public long getTotal() { return total; }
        public long getOpen() { return open; }
        public long getInProgress() { return inProgress; }
        public long getCompleted() { return completed; }
    }
}
