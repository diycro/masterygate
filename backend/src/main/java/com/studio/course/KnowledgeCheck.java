package com.studio.course;

import java.util.List;

/**
 * A short multiple-choice check at the end of a lesson. The correct index and explanation are
 * authoring data — grading happens server-side (see CourseService.gradeCheck) so the answer key
 * never ships to the client before it's submitted, same trust model as the mastery gate.
 */
public record KnowledgeCheck(String question, List<String> options, int correctIndex, String explanation) {
    public static KnowledgeCheck of(String question, int correctIndex, String explanation, String... options) {
        return new KnowledgeCheck(question, List.of(options), correctIndex, explanation);
    }
}
