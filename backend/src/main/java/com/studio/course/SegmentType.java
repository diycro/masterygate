package com.studio.course;

/** How the player renders and styles a segment. */
public enum SegmentType {
    /** Plain narrated explanation text. */
    CONCEPT,
    /** Narrated explanation alongside a rendered Diagram. */
    DIAGRAM,
    /** Narrated explanation alongside an illustrative code snippet. */
    CODE,
    /** A short real-world scenario/example, narrated like a mini story. */
    STORY,
    /** An "Interview Corner" callout — how this concept shows up in a real interview round. */
    CALLOUT
}
