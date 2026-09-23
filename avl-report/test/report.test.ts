import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { readAllureResults } from "../src/allureResults.js";
import { buildComplianceSummary } from "../src/report.js";

const fixturesDir = join(
  dirname(fileURLToPath(import.meta.url)),
  "..",
  "fixtures",
  "allure-results",
);

describe("buildComplianceSummary", () => {
  it("summarizes coverage, breakdowns and missing labels from real results", async () => {
    const results = await readAllureResults(fixturesDir);

    const summary = buildComplianceSummary(results, ["team", "layer"]);

    expect(summary.totalTests).toBe(3);
    expect(summary.coverage.team).toEqual({ count: 2, percentage: 66.7 });
    expect(summary.coverage.layer).toEqual({ count: 2, percentage: 66.7 });
    expect(summary.coverage.priority).toEqual({ count: 0, percentage: 0 });
    expect(summary.byTeam).toEqual({ payments: 1, platform: 1 });
    expect(summary.byLayer).toEqual({ API: 1, UNIT: 1 });
    expect(summary.missing).toEqual([
      { testName: "sample.CheckoutApiTest.missingTeam", missingLabels: ["team"] },
      { testName: "sample.BillingExportTest.missingLayer", missingLabels: ["layer"] },
    ]);
  });

  it("returns zeroed coverage for an empty result set", () => {
    const summary = buildComplianceSummary([], ["team", "layer"]);

    expect(summary.totalTests).toBe(0);
    expect(summary.coverage.team).toEqual({ count: 0, percentage: 0 });
    expect(summary.missing).toEqual([]);
  });
});
