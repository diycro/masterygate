package com.studio.code;

/**
 * A live-coding problem. {@code harnessTemplate} is a complete, compilable Java source file for a
 * public class named "Solution" with the placeholder {@code {{USER_METHOD}}} where the learner's
 * method goes, plus a main() that runs hardcoded test cases and prints "TEST n: PASS/FAIL ..." lines
 * followed by "RESULT: x/y". Every harness here has been manually verified (compiled + run with a
 * correct reference solution, and separately with a deliberately wrong one) before being wired in.
 */
public record CodeProblem(String id, String moduleId, String title, String description,
                          String starterCode, String harnessTemplate) {}
