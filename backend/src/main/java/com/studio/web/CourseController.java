package com.studio.web;

import com.studio.course.CourseService;
import com.studio.course.CourseView;
import com.studio.course.InterviewPlaybook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/api/course/{moduleId}")
    public CourseView getCourse(@PathVariable String moduleId, @RequestParam(required = false) Long userId) {
        return courseService.getCourse(moduleId, userId);
    }

    public record PositionRequest(Long userId, String moduleId, String lessonId, int segmentIndex) {}

    @PostMapping("/api/course/position")
    public void savePosition(@RequestBody PositionRequest req) {
        if (req.userId() == null) return;
        courseService.savePosition(req.userId(), req.moduleId(), req.lessonId(), req.segmentIndex());
    }

    public record CheckRequest(Long userId, String moduleId, String lessonId, List<Integer> answers) {}

    @PostMapping("/api/course/check")
    public CourseService.GradeChecksResponse gradeChecks(@RequestBody CheckRequest req) {
        return courseService.gradeChecks(req.userId(), req.moduleId(), req.lessonId(), req.answers());
    }

    @GetMapping("/api/course/playbook/{topicId}")
    public InterviewPlaybook playbook(@PathVariable String topicId) {
        return courseService.playbook(topicId);
    }
}
