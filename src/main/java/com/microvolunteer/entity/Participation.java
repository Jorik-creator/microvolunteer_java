package com.microvolunteer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Participation entity representing volunteer participation in tasks.
 */
@Entity
@Table(name = "participations")
public class Participation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    @NotNull(message = "Volunteer is required")
    private User volunteer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @NotNull(message = "Task is required")
    private Task task;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column
    private String notes;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime leftAt;
    
    // Constructors
    public Participation() {}
    
    public Participation(User volunteer, Task task) {
        this.volunteer = volunteer;
        this.task = task;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getVolunteer() {
        return volunteer;
    }
    
    public void setVolunteer(User volunteer) {
        this.volunteer = volunteer;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLeftAt() {
        return leftAt;
    }
    
    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }
}
