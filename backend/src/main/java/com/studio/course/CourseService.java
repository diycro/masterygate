package com.studio.course;

import com.studio.persist.CourseProgressEntity;
import com.studio.persist.CourseProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseCatalog catalog;
    private final CourseProgressRepository repo;

    public CourseService(CourseCatalog catalog, CourseProgressRepository repo) {
        this.catalog = catalog;
        this.repo = repo;
    }

    public boolean hasCourse(String moduleId) {
        return catalog.hasCourse(moduleId);
    }

    @Transactional(readOnly = true)
    public CourseView getCourse(String moduleId, Long userId) {
        List<CourseLesson> lessons = catalog.lessonsFor(moduleId);
        Map<String, CourseProgressEntity> progress = userId == null ? Map.of() :
            repo.findByUserIdAndModuleId(userId, moduleId).stream()
                .collect(Collectors.toMap(CourseProgressEntity::getLessonId, e -> e));

        List<CourseView.LessonView> views = new ArrayList<>();
        boolean allComplete = !lessons.isEmpty();
        for (CourseLesson l : lessons) {
            CourseProgressEntity p = progress.get(l.id());
            boolean completed = p != null && p.isCompleted();
            int lastSegment = p != null ? p.getLastSegmentIndex() : 0;
            if (!completed) allComplete = false;
            List<CourseView.CheckView> checkViews = l.checks().stream()
                .map(c -> new CourseView.CheckView(c.question(), c.options()))
                .toList();
            views.add(new CourseView.LessonView(l.id(), l.order(), l.title(), l.subtitle(), l.minutes(),
                l.segments(), checkViews, completed, lastSegment));
        }
        return new CourseView(moduleId, views, allComplete);
    }

    /** True when this module has no authored course (nothing to gate) or the user has finished every lesson. */
    @Transactional(readOnly = true)
    public boolean isCourseComplete(String moduleId, Long userId) {
        List<CourseLesson> lessons = catalog.lessonsFor(moduleId);
        if (lessons.isEmpty()) return true;
        if (userId == null) return false;
        for (CourseLesson l : lessons) {
            Optional<CourseProgressEntity> p = repo.findByUserIdAndLessonId(userId, l.id());
            if (p.isEmpty() || !p.get().isCompleted()) return false;
        }
        return true;
    }

    @Transactional
    public void savePosition(Long userId, String moduleId, String lessonId, int segmentIndex) {
        CourseProgressEntity e = repo.findByUserIdAndLessonId(userId, lessonId).orElseGet(CourseProgressEntity::new);
        e.setUserId(userId);
        e.setModuleId(moduleId);
        e.setLessonId(lessonId);
        e.setLastSegmentIndex(segmentIndex);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
    }

    public record CheckResult(boolean correct, int correctIndex, String explanation) {}
    public record GradeChecksResponse(List<CheckResult> results, boolean allCorrect, boolean lessonCompleted) {}

    @Transactional
    public GradeChecksResponse gradeChecks(Long userId, String moduleId, String lessonId, List<Integer> answers) {
        CourseLesson lesson = catalog.lesson(moduleId, lessonId);
        if (lesson == null) throw new IllegalArgumentException("Unknown lesson: " + lessonId);
        List<KnowledgeCheck> checks = lesson.checks();

        List<CheckResult> results = new ArrayList<>();
        boolean allCorrect = true;
        for (int i = 0; i < checks.size(); i++) {
            KnowledgeCheck c = checks.get(i);
            Integer given = i < answers.size() ? answers.get(i) : null;
            boolean correct = given != null && given == c.correctIndex();
            if (!correct) allCorrect = false;
            results.add(new CheckResult(correct, c.correctIndex(), c.explanation()));
        }

        boolean lessonCompleted = allCorrect;
        if (lessonCompleted && userId != null) {
            CourseProgressEntity e = repo.findByUserIdAndLessonId(userId, lessonId).orElseGet(CourseProgressEntity::new);
            e.setUserId(userId);
            e.setModuleId(moduleId);
            e.setLessonId(lessonId);
            e.setCompleted(true);
            e.setUpdatedAt(Instant.now());
            repo.save(e);
        }
        return new GradeChecksResponse(results, allCorrect, lessonCompleted);
    }

    public InterviewPlaybook playbook(String topicId) {
        return catalog.playbook(topicId);
    }
}
