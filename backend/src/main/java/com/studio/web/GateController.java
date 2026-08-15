package com.studio.web;

import com.studio.exam.GateService;
import com.studio.exam.GateSession;
import com.studio.exam.GradeResult;
import com.studio.exam.ModuleCatalog;
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

    public GateController(GateService gate) {
        this.gate = gate;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestParam String moduleId,
                                     @RequestParam(defaultValue = "false") boolean dynamic) {
        GateSession s = gate.start(moduleId, dynamic);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionId", s.id);
        out.put("moduleId", s.moduleId);
        out.put("total", s.questions.size());
        out.put("index", s.index);
        out.put("question", s.current().text());
        return out;
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
        } else {
            out.put("index", s.index);
            out.put("total", s.questions.size());
            out.put("question", s.current().text());
        }
        return out;
    }
}
