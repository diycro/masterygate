package com.studio.course;

import java.util.List;

/** One round of a real interview loop: what it's called, how long, what it actually tests. */
public record InterviewRound(String name, String duration, String whatTheyTest,
                              List<String> sampleQuestions, String proTip) {
}
