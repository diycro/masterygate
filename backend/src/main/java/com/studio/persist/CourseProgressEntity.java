package com.studio.persist;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/** Per-user completion + resume position for one interactive-course lesson. */
@Entity
@Table(name = "course_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "lessonId"}))
public class CourseProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String moduleId;
    private String lessonId;
    private boolean completed;
    private int lastSegmentIndex;
    private Instant updatedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getLastSegmentIndex() { return lastSegmentIndex; }
    public void setLastSegmentIndex(int lastSegmentIndex) { this.lastSegmentIndex = lastSegmentIndex; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
