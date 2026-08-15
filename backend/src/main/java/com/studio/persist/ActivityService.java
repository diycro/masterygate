package com.studio.persist;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Tracks real daily activity per user and computes a genuine (not fabricated) streak from it. */
@Service
public class ActivityService {

    private final ActivityRepository repo;

    public ActivityService(ActivityRepository repo) {
        this.repo = repo;
    }

    /** Mark the user active today (idempotent — at most one row per user per day). */
    @Transactional
    public void recordToday(Long userId) {
        if (userId == null) return;
        LocalDate today = LocalDate.now();
        if (repo.findByUserIdAndDay(userId, today).isPresent()) return;
        ActivityEntity e = new ActivityEntity();
        e.setUserId(userId);
        e.setDay(today);
        try {
            repo.save(e);
        } catch (Exception racedWithAnotherRequest) {
            // another concurrent request already recorded today — fine, ignore.
        }
    }

    /** Consecutive days of activity ending today or yesterday (a streak isn't broken until a day is fully missed). */
    @Transactional(readOnly = true)
    public int currentStreak(Long userId) {
        List<ActivityEntity> days = repo.findByUserIdOrderByDayDesc(userId);
        if (days.isEmpty()) return 0;
        Set<LocalDate> set = new HashSet<>();
        for (ActivityEntity a : days) set.add(a.getDay());

        LocalDate cursor = LocalDate.now();
        if (!set.contains(cursor)) {
            cursor = cursor.minusDays(1);       // today not logged yet — streak can still be "alive" via yesterday
            if (!set.contains(cursor)) return 0;
        }
        int streak = 0;
        while (set.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
