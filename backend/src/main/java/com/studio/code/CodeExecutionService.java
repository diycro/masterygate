package com.studio.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles and runs a learner's Java code against a problem's fixed test harness, using the SAME JDK
 * that runs this Spring app (via java.home) — no separate toolchain to install.
 *
 * SECURITY NOTE: this executes arbitrary user-submitted code as a real OS process. There is no
 * sandboxing beyond a hard timeout and a fresh temp directory — the trust level is the same as running
 * code in your own IDE. This is appropriate for a personal, local learning tool and must NOT be exposed
 * on a shared or public-facing server without real sandboxing (containers, seccomp, etc.).
 */
@Service
public class CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final int MAX_CODE_CHARS = 20_000;
    private static final long COMPILE_TIMEOUT_SEC = 15;
    private static final long RUN_TIMEOUT_SEC = 8;
    private static final int MAX_OUTPUT_CHARS = 50_000;

    private static final Pattern TEST_LINE = Pattern.compile("^TEST (\\d+): (PASS|FAIL)(.*)$");
    private static final Pattern RESULT_LINE = Pattern.compile("^RESULT: (\\d+)/(\\d+)$");

    public record TestOutcome(int index, boolean pass, String detail) {}
    public record RunResult(boolean compiled, String compileError, boolean timedOut,
                            List<TestOutcome> tests, int passCount, int totalCount, String rawOutput) {}

    public RunResult run(CodeProblem problem, String userCode) {
        if (userCode == null || userCode.isBlank()) {
            return new RunResult(false, "No code submitted.", false, List.of(), 0, 0, "");
        }
        if (userCode.length() > MAX_CODE_CHARS) {
            return new RunResult(false, "Code is too long (max " + MAX_CODE_CHARS + " characters).", false, List.of(), 0, 0, "");
        }

        String fullSource = problem.harnessTemplate().replace("{{USER_METHOD}}", userCode);
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("studio-code-");
        } catch (IOException e) {
            return new RunResult(false, "Server could not create a temp directory: " + e.getMessage(), false, List.of(), 0, 0, "");
        }

        try {
            Path sourceFile = tempDir.resolve("Solution.java");
            Files.writeString(sourceFile, fullSource, StandardCharsets.UTF_8);

            String javaHome = System.getProperty("java.home");
            String javac = javaHome + File.separator + "bin" + File.separator + "javac";
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

            ProcRes compile = runProcess(new String[]{javac, "-d", tempDir.toString(), sourceFile.toString()},
                    tempDir, COMPILE_TIMEOUT_SEC);
            if (compile.timedOut) {
                return new RunResult(false, "Compilation timed out.", true, List.of(), 0, 0, "");
            }
            if (compile.exitCode != 0) {
                return new RunResult(false, cleanCompileError(compile.output, sourceFile.toString()), false, List.of(), 0, 0, "");
            }

            ProcRes exec = runProcess(new String[]{javaBin, "-cp", tempDir.toString(), "Solution"}, tempDir, RUN_TIMEOUT_SEC);
            if (exec.timedOut) {
                return new RunResult(true, null, true, List.of(),
                        0, 0, "Execution timed out after " + RUN_TIMEOUT_SEC + "s — check for an infinite loop.");
            }

            return parseOutput(exec.output);
        } catch (Exception e) {
            log.warn("Code execution failed", e);
            return new RunResult(false, "Execution failed: " + e.getMessage(), false, List.of(), 0, 0, "");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private RunResult parseOutput(String output) {
        List<TestOutcome> tests = new ArrayList<>();
        int passCount = 0, totalCount = 0;
        for (String line : output.split("\\R")) {
            Matcher tm = TEST_LINE.matcher(line.trim());
            if (tm.matches()) {
                int idx = Integer.parseInt(tm.group(1));
                boolean pass = tm.group(2).equals("PASS");
                tests.add(new TestOutcome(idx, pass, tm.group(3).trim()));
                continue;
            }
            Matcher rm = RESULT_LINE.matcher(line.trim());
            if (rm.matches()) {
                passCount = Integer.parseInt(rm.group(1));
                totalCount = Integer.parseInt(rm.group(2));
            }
        }
        String cleanOutput = output.lines()
                .filter(l -> !l.startsWith("Picked up JAVA_TOOL_OPTIONS") && !l.startsWith("Picked up _JAVA_OPTIONS"))
                .reduce((a, b) -> a + "\n" + b).orElse(output);
        if (totalCount == 0 && tests.isEmpty()) {
            // Harness ran but produced no recognizable output — likely the user's code threw at runtime.
            return new RunResult(true, null, false, List.of(), 0, 0, cleanOutput);
        }
        return new RunResult(true, null, false, tests, passCount, totalCount, cleanOutput);
    }

    /**
     * Strip the temp-file's absolute path out of javac's error text, and drop the JVM's own
     * "Picked up JAVA_TOOL_OPTIONS/_JAVA_OPTIONS" diagnostic line (printed whenever that env var is
     * set, e.g. behind a proxy) so it doesn't clutter the compile error shown to the learner.
     */
    private String cleanCompileError(String raw, String sourceFilePath) {
        String cleaned = raw.lines()
                .filter(l -> !l.startsWith("Picked up JAVA_TOOL_OPTIONS") && !l.startsWith("Picked up _JAVA_OPTIONS"))
                .reduce((a, b) -> a + "\n" + b).orElse(raw);
        return cleaned.replace(sourceFilePath, "Solution.java");
    }

    private record ProcRes(int exitCode, String output, boolean timedOut) {}

    /**
     * Runs a process with a HARD timeout. The output is drained on a separate daemon thread so a
     * submission that loops forever while printing (a very plausible real submission — an infinite
     * loop with a debug print inside it) can't starve the timeout check: waitFor() on this thread is
     * never blocked behind reading the child's stdout.
     */
    private ProcRes runProcess(String[] cmd, Path workDir, long timeoutSec) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        StringBuilder sb = new StringBuilder();
        Object lock = new Object();
        Thread reader = new Thread(() -> {
            try (InputStream is = proc.getInputStream()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) {
                    synchronized (lock) {
                        if (sb.length() < MAX_OUTPUT_CHARS) {
                            sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (IOException ignored) {
                // stream closed because we forcibly destroyed the process — expected on timeout
            }
        }, "code-exec-reader");
        reader.setDaemon(true);
        reader.start();

        boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            reader.join(2000);   // give the reader a moment to notice the closed stream and stop
            synchronized (lock) { return new ProcRes(-1, sb.toString(), true); }
        }
        reader.join(2000);       // process exited; make sure we've drained whatever it printed
        synchronized (lock) { return new ProcRes(proc.exitValue(), sb.toString(), false); }
    }

    private void deleteRecursively(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}
