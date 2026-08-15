package com.studio.web;

import com.studio.course.CourseService;
import com.studio.exam.GateService;
import com.studio.exam.GateSession;
import com.studio.exam.GradeResult;
import com.studio.exam.ModuleCatalog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** REST surface for the AI examiner: list gate modules, start a gate, submit answers, get graded. */
@RestController
@RequestMapping("/api/gate")
public class GateController {

    private final GateService gate;
    private final com.studio.persist.ProgressService progressService;
    private final com.studio.persist.ActivityService activityService;
    private final CourseService courseService;

    public GateController(GateService gate, com.studio.persist.ProgressService progressService,
                          com.studio.persist.ActivityService activityService, CourseService courseService) {
        this.gate = gate;
        this.progressService = progressService;
        this.activityService = activityService;
        this.courseService = courseService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start(@RequestParam String moduleId,
                                     @RequestParam(defaultValue = "false") boolean dynamic,
                                     @RequestParam(required = false) Long userId) {
        // Modules with an authored interactive course require it finished first — only enforced
        // when we can identify the user and the module actually has a course to finish.
        if (userId != null && courseService.hasCourse(moduleId) && !courseService.isCourseComplete(moduleId, userId)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Finish the course for this module before starting the mastery gate."));
        }
        activityService.recordToday(userId);
        GateSession s = gate.start(moduleId, dynamic, userId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionId", s.id);
        out.put("moduleId", s.moduleId);
        out.put("total", s.questions.size());
        out.put("index", s.index);
        out.put("question", s.current().text());
        return ResponseEntity.ok(out);
    }

    public record AnswerRequest(String sessionId, String answer) {}

    @PostMapping("/answer")
    public Map<String, Object> answer(@RequestBody AnswerRequest req) {
        GateSession s = gate.get(req.sessionId());
        GradeResult grade = gate.grade(s, req.answer());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("grade", grade);
        out.put("done", s.done());
        if (s.done()) {
            out.put("passed", s.passed());
            out.put("avgScore", s.avgScore());
            if (s.passed() && s.userId != null) {
                progressService.recordPass(s.userId, s.topicId, s.moduleId, s.avgScore());
            }
        } else {
            out.put("index", s.index);
            out.put("total", s.questions.size());
            out.put("question", s.current().text());
        }
        return out;
    }
}
