package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.TaskRequest;
import org.example.dto.TaskResponse;
import org.example.dto.TaskUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.UnauthorizedException;
import org.example.model.*;
import org.example.repository.CategoryRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.example.util.EntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;

    public Page<TaskResponse> getAllTasks(String title, String location, Long categoryId,
                                         TaskStatus status, LocalDateTime startDateFrom,
                                         LocalDateTime startDateTo, Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(entityMapper::toTaskResponse);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return entityMapper.toTaskResponse(task);
    }

    public Page<TaskResponse> getTasksByCreator(Long creatorId, Pageable pageable) {
        return taskRepository.findByCreatorId(creatorId, pageable)
                .map(entityMapper::toTaskResponse);
    }

    public Page<TaskResponse> getTasksByParticipant(Long userId, Pageable pageable) {
        return taskRepository.findTasksByParticipantId(userId, pageable)
                .map(entityMapper::toTaskResponse);
    }

    public Page<TaskResponse> getTasksByCreatorUsername(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return getTasksByCreator(user.getId(), pageable);
    }

    public Page<TaskResponse> getTasksByParticipantUsername(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return getTasksByParticipant(user.getId(), pageable);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Task task = entityMapper.toTask(request);
        task.setCreator(creator);
        task.setCategory(category);

        Task savedTask = taskRepository.save(task);
        return entityMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!task.getCreator().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only update your own tasks");
        }

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new BadRequestException("Only open tasks can be updated");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            task.setCategory(category);
        }

        if (request.getEndDate() != null && request.getStartDate() != null &&
            request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        entityMapper.updateTaskFromRequest(task, request);
        Task updatedTask = taskRepository.save(task);
        return entityMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!task.getCreator().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only delete your own tasks");
        }

        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot delete task that is in progress");
        }

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse joinTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getUserType() != UserType.VOLUNTEER) {
            throw new BadRequestException("Only volunteers can join tasks");
        }

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new BadRequestException("Can only join open tasks");
        }

        if (task.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot join your own task");
        }

        if (task.getParticipants().contains(user)) {
            throw new BadRequestException("You are already a participant in this task");
        }

        if (task.getParticipants().size() >= task.getMaxParticipants()) {
            throw new BadRequestException("Task has reached maximum participants");
        }

        task.getParticipants().add(user);

        if (task.getParticipants().size() == task.getMaxParticipants()) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        Task updatedTask = taskRepository.save(task);
        return entityMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public TaskResponse leaveTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!task.getParticipants().contains(user)) {
            throw new BadRequestException("You are not a participant in this task");
        }

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BadRequestException("Cannot leave completed tasks");
        }

        task.getParticipants().remove(user);

        if (task.getStatus() == TaskStatus.IN_PROGRESS && 
            task.getParticipants().size() < task.getMaxParticipants()) {
            task.setStatus(TaskStatus.OPEN);
        }

        Task updatedTask = taskRepository.save(task);
        return entityMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public TaskResponse completeTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCreator().getUsername().equals(username)) {
            throw new UnauthorizedException("Only task creator can mark task as completed");
        }

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BadRequestException("Only tasks in progress can be completed");
        }

        task.setStatus(TaskStatus.COMPLETED);
        Task updatedTask = taskRepository.save(task);
        return entityMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public TaskResponse cancelTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCreator().getUsername().equals(username)) {
            throw new UnauthorizedException("Only task creator can cancel task");
        }

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed tasks");
        }

        task.setStatus(TaskStatus.CANCELLED);
        Task updatedTask = taskRepository.save(task);
        return entityMapper.toTaskResponse(updatedTask);
    }

    public boolean isUserParticipant(Long taskId, Long userId) {
        return taskRepository.isUserParticipant(taskId, userId);
    }

    public Integer getTaskParticipantsCount(Long taskId) {
        return taskRepository.countParticipantsByTaskId(taskId);
    }
}