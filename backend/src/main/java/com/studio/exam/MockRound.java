package com.studio.exam;

/** One round of a mock interview: a fixed type, a question drawn from the real question banks, and a time budget. */
public class MockRound {
    public final String type;          // "DSA" | "SYSTEM_DESIGN" | "DEEP_DIVE"
    public final String label;         // human-readable round name
    public final Question question;
    public final int timeBudgetSec;
    public String answer;
    public GradeResult grade;

    public MockRound(String type, String label, Question question, int timeBudgetSec) {
        this.type = type;
        this.label = label;
        this.question = question;
        this.timeBudgetSec = timeBudgetSec;
    }
}
