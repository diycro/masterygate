package com.studio.web;

import com.studio.exam.MockInterviewService;
import com.studio.exam.MockInterviewSession;
import com.studio.exam.MockRound;
import com.studio.persist.ActivityService;
import com.studio.persist.MockInterviewResultEntity;
import com.studio.persist.MockInterviewResultRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mock")
public class MockInterviewController {

    private final MockInterviewService mockService;
    private final MockInterviewResultRepository resultRepo;
    private final ActivityService activityService;

    public MockInterviewController(MockInterviewService mockService, MockInterviewResultRepository resultRepo,
                                   ActivityService activityService) {
        this.mockService = mockService;
        this.resultRepo = resultRepo;
        this.activityService = activityService;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestParam(required = false) String focusTopic,
                                     @RequestParam(required = false) Long userId) {
        activityService.recordToday(userId);
        MockInterviewSession s = mockService.start(focusTopic, userId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionId", s.id);
        out.put("totalRounds", s.rounds.size());
        out.put("roundIndex", s.index);
        out.put("focusTopicId", s.focusTopicId);
        out.put("round", roundDto(s.current()));
        return out;
    }

    public record AnswerRequest(String sessionId, String answer) {}

    @PostMapping("/answer")
    public Map<String, Object> answer(@RequestBody AnswerRequest req) {
        MockInterviewSession s = mockService.get(req.sessionId());
        var grade = mockService.answer(s, req.answer());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("grade", grade);
        out.put("done", s.done());
        if (s.done()) {
            out.put("overallScore", s.overallScore());
            List<Map<String, Object>> rounds = new ArrayList<>();
            for (MockRound r : s.rounds) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("type", r.type);
                rm.put("label", r.label);
                rm.put("question", r.question.text());
                rm.put("answer", r.answer);
                rm.put("score", r.grade != null ? r.grade.score() : 0);
                rm.put("feedback", r.grade != null ? r.grade.feedback() : "");
                rounds.add(rm);
            }
            out.put("rounds", rounds);

            if (s.userId != null) {
                MockInterviewResultEntity e = new MockInterviewResultEntity();
                e.setUserId(s.userId);
                e.setFocusTopicId(s.focusTopicId);
                e.setDsaScore(scoreOf(s, "DSA"));
                e.setSystemDesignScore(scoreOf(s, "SYSTEM_DESIGN"));
                e.setDeepDiveScore(scoreOf(s, "DEEP_DIVE"));
                e.setOverallScore(s.overallScore());
                e.setCompletedAt(Instant.now());
                resultRepo.save(e);
            }
        } else {
            out.put("roundIndex", s.index);
            out.put("totalRounds", s.rounds.size());
            out.put("round", roundDto(s.current()));
        }
        return out;
    }

    @GetMapping("/history")
    public List<Map<String, Object>> history(@RequestParam Long userId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (MockInterviewResultEntity e : resultRepo.findByUserIdOrderByCompletedAtDesc(userId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("focusTopicId", e.getFocusTopicId());
            m.put("dsaScore", e.getDsaScore());
            m.put("systemDesignScore", e.getSystemDesignScore());
            m.put("deepDiveScore", e.getDeepDiveScore());
            m.put("overallScore", e.getOverallScore());
            m.put("completedAt", e.getCompletedAt());
            out.add(m);
        }
        return out;
    }

    private int scoreOf(MockInterviewSession s, String type) {
        return s.rounds.stream().filter(r -> r.type.equals(type) && r.grade != null)
                .findFirst().map(r -> r.grade.score()).orElse(0);
    }

    private Map<String, Object> roundDto(MockRound r) {
        if (r == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", r.type);
        m.put("label", r.label);
        m.put("question", r.question.text());
        m.put("timeBudgetSec", r.timeBudgetSec);
        return m;
    }
}
