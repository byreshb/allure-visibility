import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { readAllureResults } from "../src/allureResults.js";
import { buildComplianceSummary } from "../src/report.js";
import { renderConsole } from "../src/render/console.js";
import { renderJson } from "../src/render/json.js";
import { renderMarkdown } from "../src/render/markdown.js";

const fixturesDir = join(
  dirname(fileURLToPath(import.meta.url)),
  "..",
  "fixtures",
  "allure-results",
);

async function fixtureSummary() {
  const results = await readAllureResults(fixturesDir);
  return buildComplianceSummary(results, ["team", "layer"]);
}

describe("renderConsole", () => {
  it("includes totals, coverage and the missing-label list", async () => {
    const output = renderConsole(await fixtureSummary());

    expect(output).toContain("Total tests: 3");
    expect(output).toContain("Missing a required label (2):");
    expect(output).toContain("sample.CheckoutApiTest.missingTeam");
  });
});

describe("renderJson", () => {
  it("renders valid, parseable JSON matching the summary", async () => {
    const summary = await fixtureSummary();
    const parsed = JSON.parse(renderJson(summary)) as unknown;

    expect(parsed).toEqual(summary);
  });
});

describe("renderMarkdown", () => {
  it("renders headings, tables and the missing-label section", async () => {
    const output = renderMarkdown(await fixtureSummary());

    expect(output).toContain("# allure-visibility report");
    expect(output).toContain("| `team` | 2 | 66.7% |");
    expect(output).toContain("## Missing a required label (2)");
  });
});
