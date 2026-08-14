package com.studio.exam;

import java.util.List;

/** One free-text gate question plus the key points a correct answer should cover (used by the grader). */
public record Question(String id, String text, List<String> keyPoints) {}
