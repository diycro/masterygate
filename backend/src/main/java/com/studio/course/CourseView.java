package com.studio.course;

import java.util.List;

/** What the client sees for a lesson — the answer key is stripped until a check is submitted. */
public record CourseView(String moduleId, List<LessonView> lessons, boolean courseComplete) {

    public record CheckView(String question, List<String> options) {}

    public record LessonView(String id, int order, String title, String subtitle, int minutes,
                              List<CourseSegment> segments, List<CheckView> checks,
                              boolean completed, int lastSegmentIndex) {}
}
