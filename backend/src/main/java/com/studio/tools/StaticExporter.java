package com.studio.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.studio.code.CodeProblemCatalog;
import com.studio.course.CourseCatalog;
import com.studio.exam.InterviewQASeedData;
import com.studio.exam.ModuleCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One-off exporter: dumps the app's hardcoded curriculum/course/Q&A/code-problem catalogs to static
 * JSON files for the fully client-side (no-backend) build. Every catalog class here has a zero-argument
 * constructor with no Spring dependencies, so this runs as a plain `java` program — no Spring context,
 * no database, no running server needed.
 *
 * Run with: java -cp target/classes:<jackson+deps on classpath> com.studio.tools.StaticExporter <outDir>
 *
 * NOTE ON EXPOSED DATA: unlike the REST API (which strips gate-question key points and knowledge-check
 * answer keys until a submission), this export includes EVERYTHING — because grading now happens
 * entirely in the browser, the rubric has to ship to the browser too. That's an accepted trade-off for
 * a personal, free, static deployment (same trust model as running the grading logic in your own IDE).
 */
public class StaticExporter {

    public static void main(String[] args) throws Exception {
        Path outDir = Path.of(args.length > 0 ? args[0] : "frontend/public/data");
        Files.createDirectories(outDir);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ModuleCatalog moduleCatalog = new ModuleCatalog();
        CourseCatalog courseCatalog = new CourseCatalog();
        CodeProblemCatalog codeCatalog = new CodeProblemCatalog();

        // ---- topics.json ----
        List<Map<String, Object>> topics = new ArrayList<>();
        for (ModuleCatalog.Topic t : moduleCatalog.topics()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.id());
            m.put("title", t.title());
            m.put("subtitle", t.subtitle());
            m.put("moduleCount", moduleCatalog.modulesFor(t.id()).size());
            topics.add(m);
        }
        write(mapper, outDir.resolve("topics.json"), topics);

        // ---- modules.json : { [topicId]: Module[] } (full data incl. gate questions/keyPoints) ----
        Map<String, Object> modulesByTopic = new LinkedHashMap<>();
        for (ModuleCatalog.Topic t : moduleCatalog.topics()) {
            List<Map<String, Object>> mods = new ArrayList<>();
            for (ModuleCatalog.Module mod : moduleCatalog.modulesFor(t.id())) {
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("id", mod.id());
                mm.put("topicId", mod.topicId());
                mm.put("order", mod.order());
                mm.put("title", mod.title());
                mm.put("objectives", mod.objectives());
                mm.put("interviewFocus", mod.interviewFocus());
                mm.put("resources", mod.resources());
                mm.put("questionCount", mod.questions().size());
                mm.put("questions", mod.questions());
                mods.add(mm);
            }
            modulesByTopic.put(t.id(), mods);
        }
        write(mapper, outDir.resolve("modules.json"), modulesByTopic);

        // ---- qa.json : { [moduleId]: {id, question, answer, explanation, frequency, source}[] } ----
        Map<String, Object> qaByModule = new LinkedHashMap<>();
        int[] idSeq = {1};
        InterviewQASeedData.all().forEach((moduleId, questions) -> {
            List<Map<String, Object>> items = new ArrayList<>();
            for (InterviewQASeedData.SeedQA q : questions) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", idSeq[0]++);
                m.put("question", q.question());
                m.put("answer", q.answer());
                m.put("explanation", q.explanation());
                m.put("frequency", q.frequency());
                m.put("source", "curated");
                items.add(m);
            }
            qaByModule.put(moduleId, items);
        });
        write(mapper, outDir.resolve("qa.json"), qaByModule);

        // ---- courses.json : { lessonsByModule: {[moduleId]: CourseLesson[]}, playbookByTopic: {[topicId]: Playbook} } ----
        Map<String, Object> lessonsByModule = new LinkedHashMap<>();
        for (String topicId : modulesByTopic.keySet()) {
            for (ModuleCatalog.Module mod : moduleCatalog.modulesFor(topicId)) {
                var lessons = courseCatalog.lessonsFor(mod.id());
                if (lessons != null && !lessons.isEmpty()) lessonsByModule.put(mod.id(), lessons);
            }
        }
        Map<String, Object> playbookByTopic = new LinkedHashMap<>();
        for (ModuleCatalog.Topic t : moduleCatalog.topics()) {
            var pb = courseCatalog.playbook(t.id());
            if (pb != null) playbookByTopic.put(t.id(), pb);
        }
        Map<String, Object> courses = new LinkedHashMap<>();
        courses.put("lessonsByModule", lessonsByModule);
        courses.put("playbookByTopic", playbookByTopic);
        write(mapper, outDir.resolve("courses.json"), courses);

        // ---- code-problems.json : { [moduleId]: CodeProblem } ----
        Map<String, Object> codeByModule = new LinkedHashMap<>();
        for (String topicId : modulesByTopic.keySet()) {
            for (ModuleCatalog.Module mod : moduleCatalog.modulesFor(topicId)) {
                var p = codeCatalog.forModule(mod.id());
                if (p != null) codeByModule.put(mod.id(), p);
            }
        }
        write(mapper, outDir.resolve("code-problems.json"), codeByModule);

        System.out.println("Exported static content to " + outDir.toAbsolutePath());
        System.out.println("  topics: " + topics.size());
        System.out.println("  modules: " + modulesByTopic.values().stream().mapToInt(v -> ((List<?>) v).size()).sum());
        System.out.println("  qa modules: " + qaByModule.size() + ", total questions: "
                + qaByModule.values().stream().mapToInt(v -> ((List<?>) v).size()).sum());
        System.out.println("  courses: " + lessonsByModule.size() + " modules, " + playbookByTopic.size() + " playbooks");
        System.out.println("  code problems: " + codeByModule.size());
    }

    private static void write(ObjectMapper mapper, Path path, Object data) throws IOException {
        mapper.writeValue(path.toFile(), data);
    }
}
