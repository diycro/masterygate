package com.studio.course;

/**
 * One "scene" in a lesson — the unit the player shows and narrates one at a time, like a slide
 * in a narrated video. `narration` is both the on-screen text and what gets read aloud.
 */
public record CourseSegment(String id, SegmentType type, String title, String narration,
                             Diagram diagram, String code, String codeLang) {

    public static CourseSegment concept(String id, String title, String narration) {
        return new CourseSegment(id, SegmentType.CONCEPT, title, narration, null, null, null);
    }
    public static CourseSegment diagram(String id, String title, String narration, Diagram diagram) {
        return new CourseSegment(id, SegmentType.DIAGRAM, title, narration, diagram, null, null);
    }
    public static CourseSegment code(String id, String title, String narration, String codeLang, String code) {
        return new CourseSegment(id, SegmentType.CODE, title, narration, null, code, codeLang);
    }
    public static CourseSegment story(String id, String title, String narration) {
        return new CourseSegment(id, SegmentType.STORY, title, narration, null, null, null);
    }
    public static CourseSegment interviewCorner(String id, String title, String narration) {
        return new CourseSegment(id, SegmentType.CALLOUT, title, narration, null, null, null);
    }
}
