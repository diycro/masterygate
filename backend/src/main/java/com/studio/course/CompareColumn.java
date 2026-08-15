package com.studio.course;

import java.util.List;

/** One column of a "compare" diagram: a heading plus its bullet points. */
public record CompareColumn(String heading, List<String> points) {
    public static CompareColumn of(String heading, String... points) {
        return new CompareColumn(heading, List.of(points));
    }
}
