package com.studio.persist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockInterviewResultRepository extends JpaRepository<MockInterviewResultEntity, Long> {
    List<MockInterviewResultEntity> findByUserIdOrderByCompletedAtDesc(Long userId);
}
