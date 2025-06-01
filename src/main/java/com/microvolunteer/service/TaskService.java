package com.microvolunteer.service;

import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<Task> getAllTasks() {
        log.info("Fetching all tasks");
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        log.info("Fetching task with ID: {}", id);
        return taskRepository.findById(id);
    }

    public Page<Task> getTasksByStatus(TaskStatus status, Pageable pageable) {
        log.info("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status, pageable);
    }

    public List<Task> getTasksByCategory(Long categoryId) {
        log.info("Fetching tasks for category ID: {}", categoryId);
        return taskRepository.findByCategoryId(categoryId);
    }

    public List<Task> getTasksByCreator(Long creatorId) {
        log.info("Fetching tasks created by user ID: {}", creatorId);
        return taskRepository.findByCreatorId(creatorId);
    }

    public Page<Task> searchTasks(String query, Long categoryId, TaskStatus status, 
                                 LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        log.info("Searching tasks with query: {}, categoryId: {}, status: {}", query, categoryId, status);
        return taskRepository.searchTasks(query, categoryId, status, dateFrom, dateTo, pageable);
    }

    @Transactional
    public Task createTask(Task task) {
        log.info("Creating new task: {}", task.getTitle());
        
        // Валідації
        validateTaskCreation(task);
        
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Transactional
    public Optional<Task> updateTask(Long id, Task updatedTask) {
        log.info("Updating task with ID: {}", id);
        return taskRepository.findById(id)
                .map(task -> {
                    validateTaskUpdate(task, updatedTask);
                    
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    task.setLocation(updatedTask.getLocation());
                    task.setRequiredSkills(updatedTask.getRequiredSkills());
                    task.setMaxParticipants(updatedTask.getMaxParticipants());
                    task.setScheduledAt(updatedTask.getScheduledAt());
                    task.setUpdatedAt(LocalDateTime.now());
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public boolean deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            validateTaskDeletion(task);
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Task> updateTaskStatus(Long id, TaskStatus status) {
        log.info("Updating task status for ID: {} to {}", id, status);
        return taskRepository.findById(id)
                .map(task -> {
                    validateStatusChange(task, status);
                    task.setStatus(status);
                    task.setUpdatedAt(LocalDateTime.now());
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public Optional<Task> completeTask(Long taskId, Long userId) {
        log.info("Completing task {} by user {}", taskId, userId);
        
        return taskRepository.findById(taskId)
                .map(task -> {
                    validateTaskCompletion(task, userId);
                    task.setStatus(TaskStatus.COMPLETED);
                    task.setUpdatedAt(LocalDateTime.now());
                    return taskRepository.save(task);
                });
    }

    // Приватні методи валідації
    private void validateTaskCreation(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new BusinessException("Task title cannot be empty");
        }
        
        if (task.getMaxParticipants() != null && task.getMaxParticipants() <= 0) {
            throw new BusinessException("Max participants must be positive");
        }
        
        if (task.getScheduledAt() != null && task.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Scheduled time cannot be in the past");
        }
        
        if (task.getCreator() == null) {
            throw new BusinessException("Task must have a creator");
        }
    }

    private void validateTaskUpdate(Task existingTask, Task updatedTask) {
        if (existingTask.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessException("Cannot update completed task");
        }
        
        if (task.getMaxParticipants() != null && updatedTask.getMaxParticipants() < currentParticipants) {
            throw new BusinessException("Cannot reduce max participants below current participant count");
        }
    }

    private void validateTaskDeletion(Task task) {
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BusinessException("Cannot delete task that is in progress");
        }
        
        int participants = taskRepository.countParticipantsByTaskId(task.getId());
        if (participants > 0) {
            throw new BusinessException("Cannot delete task with participants");
        }
    }

    private void validateStatusChange(Task task, TaskStatus newStatus) {
        TaskStatus currentStatus = task.getStatus();
        
        switch (currentStatus) {
            case OPEN:
                if (newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.COMPLETED) {
                    throw new BusinessException("Open task can only be moved to IN_PROGRESS or COMPLETED");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != TaskStatus.COMPLETED && newStatus != TaskStatus.OPEN) {
                    throw new BusinessException("In progress task can only be moved to COMPLETED or back to OPEN");
                }
                break;
            case COMPLETED:
                throw new BusinessException("Completed task status cannot be changed");
        }
    }

    private void validateTaskCompletion(Task task, Long userId) {
        if (!task.getCreator().getId().equals(userId)) {
            throw new BusinessException("Only task creator can complete the task");
        }
        
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessException("Task is already completed");
        }
    }
}
