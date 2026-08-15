package com.studio.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Whether a specific curated resource (free/paid course link) within a module has been marked
 * complete by a user. Keyed by URL since curated resources don't have a stable numeric id.
 */
@Entity
@Table(name = "resource_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "moduleId", "resourceKey"}))
public class ResourceProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String moduleId;

    @Column(name = "resourceKey", length = 500)
    private String resourceKey;   // the resource's URL

    private boolean done;
    private Instant updatedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getResourceKey() { return resourceKey; }
    public void setResourceKey(String resourceKey) { this.resourceKey = resourceKey; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
