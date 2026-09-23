#!/usr/bin/env node
import { readAllureResults } from "./allureResults.js";
import { readRequiredLabels } from "./avlProperties.js";
import { buildComplianceSummary } from "./report.js";
import { renderConsole } from "./render/console.js";
import { renderJson } from "./render/json.js";
import { renderMarkdown } from "./render/markdown.js";
import type { OutputFormat } from "./types.js";

interface ParsedArgs {
  resultsDir: string;
  format: OutputFormat;
  propertiesPath: string | undefined;
}

/** Parses CLI arguments, reads the Allure results, and renders the report. */
export async function run(argv: string[]): Promise<string> {
  const { resultsDir, format, propertiesPath } = parseArgs(argv);

  const results = await readAllureResults(resultsDir);
  const requiredLabels = await readRequiredLabels(propertiesPath);
  const summary = buildComplianceSummary(results, requiredLabels);

  switch (format) {
    case "json":
      return renderJson(summary);
    case "md":
      return renderMarkdown(summary);
    default:
      return renderConsole(summary);
  }
}

function parseArgs(argv: string[]): ParsedArgs {
  const positional: string[] = [];
  let format: OutputFormat = "console";
  let propertiesPath: string | undefined;

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === "--format") {
      const value = argv[i + 1];
      if (value !== "console" && value !== "json" && value !== "md") {
        throw new Error(`Unknown --format '${value ?? ""}'; expected console, json or md`);
      }
      format = value;
      i += 1;
    } else if (arg === "--config") {
      const value = argv[i + 1];
      if (value === undefined) {
        throw new Error("--config requires a path to an allure.properties file");
      }
      propertiesPath = value;
      i += 1;
    } else {
      positional.push(arg);
    }
  }

  const resultsDir = positional[0];
  if (!resultsDir) {
    throw new Error(
      "Usage: allure-visibility-report <allure-results-dir> [--format console|json|md] [--config <allure.properties>]",
    );
  }

  return { resultsDir, format, propertiesPath };
}

async function main(): Promise<void> {
  try {
    const output = await run(process.argv.slice(2));
    console.log(output);
  } catch (error) {
    console.error(error instanceof Error ? error.message : String(error));
    process.exitCode = 1;
  }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  void main();
}
