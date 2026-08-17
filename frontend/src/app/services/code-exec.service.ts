import { Injectable } from '@angular/core';
import { RunResult, TestOutcome } from '../models';

const PISTON_BASE = 'https://emkc.org/api/v2/piston';
const TEST_LINE = /^TEST (\d+): (PASS|FAIL)(.*)$/;
const RESULT_LINE = /^RESULT: (\d+)\/(\d+)$/;

/**
 * Runs a learner's Java submission via the free, public Piston code-execution API (emkc.org) — no
 * server, no key needed. The harness template (with the learner's method spliced in) already prints
 * "TEST n: PASS/FAIL ..." and "RESULT: x/y" lines itself, so parsing here is just reading that output
 * — the same format the old server-side runner produced, ported line for line.
 */
@Injectable({ providedIn: 'root' })
export class CodeExecService {
  private javaVersion: string | null = null;

  private async resolveJavaVersion(): Promise<string> {
    if (this.javaVersion) return this.javaVersion;
    const res = await fetch(`${PISTON_BASE}/runtimes`);
    if (!res.ok) throw new Error('Could not reach the code-execution service.');
    const runtimes: Array<{ language: string; version: string; aliases: string[] }> = await res.json();
    const java = runtimes.find(r => r.language === 'java');
    this.javaVersion = java?.version || '*';
    return this.javaVersion;
  }

  async run(harnessTemplate: string, userCode: string): Promise<RunResult> {
    if (!userCode || !userCode.trim()) {
      return { compiled: false, compileError: 'No code submitted.', timedOut: false, passCount: 0, totalCount: 0, tests: [] };
    }
    if (userCode.length > 20_000) {
      return { compiled: false, compileError: 'Code is too long (max 20,000 characters).', timedOut: false, passCount: 0, totalCount: 0, tests: [] };
    }

    const fullSource = harnessTemplate.replace('{{USER_METHOD}}', userCode);
    let version: string;
    try { version = await this.resolveJavaVersion(); }
    catch (e: any) { return { compiled: false, compileError: e.message, timedOut: false, passCount: 0, totalCount: 0, tests: [] }; }

    let res: Response;
    try {
      res = await fetch(`${PISTON_BASE}/execute`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          language: 'java', version,
          files: [{ name: 'Solution.java', content: fullSource }]
        })
      });
    } catch {
      return { compiled: false, compileError: 'Could not reach the code-execution service — check your connection.', timedOut: false, passCount: 0, totalCount: 0, tests: [] };
    }
    if (!res.ok) {
      return { compiled: false, compileError: `Code-execution service error (${res.status}).`, timedOut: false, passCount: 0, totalCount: 0, tests: [] };
    }
    const data = await res.json();

    if (data.compile && data.compile.code !== 0) {
      return { compiled: false, compileError: this.cleanCompileError(data.compile.stderr || data.compile.output || 'Compilation failed.'), timedOut: false, passCount: 0, totalCount: 0, tests: [] };
    }
    const run = data.run || {};
    if (run.signal === 'SIGKILL' || run.code === 124) {
      return { compiled: true, compileError: null, timedOut: true, passCount: 0, totalCount: 0, tests: [], rawOutput: 'Execution timed out — check for an infinite loop.' };
    }
    return this.parseOutput(run.stdout || run.output || '');
  }

  private parseOutput(output: string): RunResult {
    const tests: TestOutcome[] = [];
    let passCount = 0, totalCount = 0;
    for (const rawLine of output.split(/\r?\n/)) {
      const line = rawLine.trim();
      const tm = TEST_LINE.exec(line);
      if (tm) { tests.push({ index: Number(tm[1]), pass: tm[2] === 'PASS', detail: (tm[3] || '').trim() }); continue; }
      const rm = RESULT_LINE.exec(line);
      if (rm) { passCount = Number(rm[1]); totalCount = Number(rm[2]); }
    }
    if (totalCount === 0 && tests.length === 0) {
      return { compiled: true, compileError: null, timedOut: false, passCount: 0, totalCount: 0, tests: [], rawOutput: output };
    }
    return { compiled: true, compileError: null, timedOut: false, passCount, totalCount, tests };
  }

  private cleanCompileError(raw: string): string {
    return raw.replace(/Solution\.java/g, 'Solution.java'); // placeholder for future path-stripping if Piston ever includes one
  }
}
