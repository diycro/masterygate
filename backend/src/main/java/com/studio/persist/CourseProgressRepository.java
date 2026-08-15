package com.studio.persist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgressEntity, Long> {
    List<CourseProgressEntity> findByUserIdAndModuleId(Long userId, String moduleId);
    Optional<CourseProgressEntity> findByUserIdAndLessonId(Long userId, String lessonId);
}
