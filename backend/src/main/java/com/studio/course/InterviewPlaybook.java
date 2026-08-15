package com.studio.course;

import java.util.List;

/**
 * Topic-level (not per-module) breakdown of how the real interview loop goes at a handful of
 * representative high-paying companies, plus general prep guidance. Always unlocked — it's
 * reference material, not something to gate behind lesson completion.
 */
public record InterviewPlaybook(String topicId, String title, String intro,
                                 List<CompanyTrack> companies, List<String> commonPitfalls,
                                 List<String> prepChecklist) {
}
