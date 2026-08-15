package com.studio.course;

import java.util.List;

/**
 * A small, generic diagram spec rendered client-side — data, not hand-drawn art, so every lesson
 * gets a consistent visual without one-off SVGs per concept.
 *
 * kind:
 *  - "flow"    ordered boxes connected left-to-right (a pipeline: ingest -> chunk -> embed -> ...)
 *  - "cycle"   ordered boxes that loop back to the first (the agent reason-act-observe loop)
 *  - "stack"   boxes stacked top-to-bottom (a layered architecture: client -> API -> vector DB -> LLM)
 *  - "compare" exactly two labeled columns of bullet points (RAG vs fine-tuning, HNSW vs IVFFlat)
 */
public record Diagram(String kind, String caption, List<DiagramNode> nodes, List<CompareColumn> columns) {

    public static Diagram flow(String caption, DiagramNode... nodes) {
        return new Diagram("flow", caption, List.of(nodes), null);
    }
    public static Diagram cycle(String caption, DiagramNode... nodes) {
        return new Diagram("cycle", caption, List.of(nodes), null);
    }
    public static Diagram stack(String caption, DiagramNode... nodes) {
        return new Diagram("stack", caption, List.of(nodes), null);
    }
    public static Diagram compare(String caption, CompareColumn left, CompareColumn right) {
        return new Diagram("compare", caption, null, List.of(left, right));
    }
}
