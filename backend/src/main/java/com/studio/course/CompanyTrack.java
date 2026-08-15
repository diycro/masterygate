package com.studio.course;

import java.util.List;

/** One company's (or company archetype's) full interview loop, round by round. */
public record CompanyTrack(String company, String levelNote, List<InterviewRound> rounds) {
}
