package com.studio.persist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<ProgressEntity, Long> {
    List<ProgressEntity> findByUserIdAndTopicId(Long userId, String topicId);
    Optional<ProgressEntity> findByUserIdAndTopicIdAndModuleId(Long userId, String topicId, String moduleId);
    List<ProgressEntity> findByUserId(Long userId);
    Optional<ProgressEntity> findFirstByUserIdOrderByUpdatedAtDesc(Long userId);
}
