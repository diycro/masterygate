package com.studio.persist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQARepository extends JpaRepository<InterviewQAEntity, Long> {
    List<InterviewQAEntity> findByModuleIdOrderByIdAsc(String moduleId);
    long countByModuleId(String moduleId);
    long countByModuleIdIn(List<String> moduleIds);
    boolean existsByModuleIdAndQuestion(String moduleId, String question);
}
