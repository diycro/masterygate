package com.studio.exam;

import java.util.List;

/** The LLM grader's structured verdict on one answer. Spring AI maps the model's JSON into this record. */
public record GradeResult(String verdict, int score, String feedback, List<String> missing) {}
