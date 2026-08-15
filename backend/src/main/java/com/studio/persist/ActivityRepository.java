package com.studio.persist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
    Optional<ActivityEntity> findByUserIdAndDay(Long userId, LocalDate day);
    List<ActivityEntity> findByUserIdOrderByDayDesc(Long userId);
}
