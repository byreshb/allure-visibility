import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { run } from "../src/cli.js";

const fixturesDir = join(dirname(fileURLToPath(import.meta.url)), "..", "fixtures");
const resultsDir = join(fixturesDir, "allure-results");

describe("run", () => {
  it("defaults to the console format", async () => {
    const output = await run([resultsDir]);
    expect(output).toContain("allure-visibility report");
  });

  it("renders JSON with --format json", async () => {
    const output = await run([resultsDir, "--format", "json"]);
    expect(() => {
      JSON.parse(output) as unknown;
    }).not.toThrow();
  });

  it("renders Markdown with --format md", async () => {
    const output = await run([resultsDir, "--format", "md"]);
    expect(output).toContain("# allure-visibility report");
  });

  it("reads required labels from --config", async () => {
    const output = await run([
      resultsDir,
      "--format",
      "json",
      "--config",
      join(fixturesDir, "allure.properties"),
    ]);
    const parsed = JSON.parse(output) as { requiredLabels: string[] };
    expect(parsed.requiredLabels).toEqual(["team", "layer"]);
  });

  it("rejects an unknown format", async () => {
    await expect(run([resultsDir, "--format", "yaml"])).rejects.toThrow(/Unknown --format/);
  });

  it("rejects a missing results directory argument", async () => {
    await expect(run([])).rejects.toThrow(/Usage:/);
  });

  it("rejects --config with no value", async () => {
    await expect(run([resultsDir, "--config"])).rejects.toThrow(/--config requires/);
  });
});
