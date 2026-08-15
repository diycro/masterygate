package com.studio.course;

import java.util.List;

/** One chapter of a module's course: a run of narrated segments, ending in a knowledge check. */
public record CourseLesson(String id, String moduleId, int order, String title, String subtitle,
                            int minutes, List<CourseSegment> segments, List<KnowledgeCheck> checks) {
}
