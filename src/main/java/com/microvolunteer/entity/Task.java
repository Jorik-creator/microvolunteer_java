package com.microvolunteer.entity;

import com.microvolunteer.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "max_volunteers")
    @Builder.Default
    private Integer maxVolunteers = 1;

    @Column(name = "current_volunteers")
    @Builder.Default
    private Integer currentVolunteers = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TaskImage> images = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Participation> participants = new HashSet<>();

    public boolean isPastDue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    public int getAvailableSpots() {
        return Math.max(0, maxVolunteers - currentVolunteers);
    }
}