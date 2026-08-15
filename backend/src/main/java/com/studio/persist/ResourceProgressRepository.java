package com.studio.persist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceProgressRepository extends JpaRepository<ResourceProgressEntity, Long> {
    List<ResourceProgressEntity> findByUserIdAndModuleId(Long userId, String moduleId);
    Optional<ResourceProgressEntity> findByUserIdAndModuleIdAndResourceKey(Long userId, String moduleId, String resourceKey);
}
