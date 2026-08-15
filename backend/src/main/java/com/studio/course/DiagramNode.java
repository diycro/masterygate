package com.studio.course;

/** One box in a flow/cycle/stack diagram: a short label plus an optional one-line subtext. */
public record DiagramNode(String label, String sub) {
    public DiagramNode(String label) { this(label, null); }
}
