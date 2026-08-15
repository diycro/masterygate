package com.studio.web;

import com.studio.code.CodeExecutionService;
import com.studio.code.CodeProblem;
import com.studio.code.CodeProblemCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Live-coding: fetch a module's code problem (never exposes the test harness) and run submissions. */
@RestController
@RequestMapping("/api/code")
public class CodeController {

    private final CodeProblemCatalog catalog;
    private final CodeExecutionService execService;

    public CodeController(CodeProblemCatalog catalog, CodeExecutionService execService) {
        this.catalog = catalog;
        this.execService = execService;
    }

    @GetMapping("/problem")
    public Map<String, Object> problem(@RequestParam String moduleId) {
        CodeProblem p = catalog.forModule(moduleId);
        Map<String, Object> out = new LinkedHashMap<>();
        if (p == null) { out.put("available", false); return out; }
        out.put("available", true);
        out.put("id", p.id());
        out.put("title", p.title());
        out.put("description", p.description());
        out.put("starterCode", p.starterCode());
        return out;
    }

    public record RunRequest(String moduleId, String code) {}

    @PostMapping("/run")
    public Map<String, Object> run(@RequestBody RunRequest req) {
        CodeProblem p = catalog.forModule(req.moduleId());
        Map<String, Object> out = new LinkedHashMap<>();
        if (p == null) { out.put("compiled", false); out.put("compileError", "No code problem for this module."); return out; }

        CodeExecutionService.RunResult r = execService.run(p, req.code());
        out.put("compiled", r.compiled());
        out.put("compileError", r.compileError());
        out.put("timedOut", r.timedOut());
        out.put("passCount", r.passCount());
        out.put("totalCount", r.totalCount());
        List<Map<String, Object>> tests = r.tests().stream().map(t -> {
            Map<String, Object> tm = new LinkedHashMap<>();
            tm.put("index", t.index());
            tm.put("pass", t.pass());
            tm.put("detail", t.detail());
            return tm;
        }).toList();
        out.put("tests", tests);
        if (r.compiled() && tests.isEmpty() && !r.timedOut()) {
            out.put("rawOutput", r.rawOutput());   // likely a runtime exception in user code
        }
        return out;
    }
}
