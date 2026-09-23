import { readdir, readFile } from "node:fs/promises";
import { join } from "node:path";
import type { AllureLabel, AllureTestResult } from "./types.js";

interface RawAllureResult {
  name?: unknown;
  fullName?: unknown;
  labels?: unknown;
}

/**
 * Reads every `*-result.json` file (the standard per-test Allure result files) in a directory
 * and extracts each test's name and labels.
 */
export async function readAllureResults(resultsDir: string): Promise<AllureTestResult[]> {
  const entries = await readdir(resultsDir);
  const resultFiles = entries.filter((name) => name.endsWith("-result.json")).sort();

  const results: AllureTestResult[] = [];
  for (const file of resultFiles) {
    const raw = await readFile(join(resultsDir, file), "utf8");
    const parsed = JSON.parse(raw) as RawAllureResult;

    results.push({
      name: typeof parsed.name === "string" ? parsed.name : file,
      fullName: typeof parsed.fullName === "string" ? parsed.fullName : undefined,
      labels: isLabelArray(parsed.labels) ? parsed.labels : [],
    });
  }
  return results;
}

function isLabelArray(value: unknown): value is AllureLabel[] {
  return Array.isArray(value) && value.every(isLabel);
}

function isLabel(value: unknown): value is AllureLabel {
  if (typeof value !== "object" || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return typeof candidate.name === "string" && typeof candidate.value === "string";
}
