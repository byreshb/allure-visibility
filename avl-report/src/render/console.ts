import Table from "cli-table3";
import pc from "picocolors";
import { REQUIRED_LABEL_NAMES, type ComplianceSummary } from "../types.js";

/** Renders a {@link ComplianceSummary} as a colored console table. */
export function renderConsole(summary: ComplianceSummary): string {
  const lines: string[] = [];
  lines.push(pc.bold("allure-visibility report"));
  lines.push(`Total tests: ${summary.totalTests}`);
  lines.push(`Required labels: ${summary.requiredLabels.join(", ")}`);
  lines.push("");

  const coverageTable = new Table({ head: ["Label", "Count", "Coverage"] });
  for (const label of REQUIRED_LABEL_NAMES) {
    const { count, percentage } = summary.coverage[label];
    coverageTable.push([label, String(count), colorPercentage(percentage)]);
  }
  lines.push(coverageTable.toString());
  lines.push("");

  lines.push(pc.bold("By team"));
  lines.push(breakdownTable(summary.byTeam).toString());
  lines.push("");

  lines.push(pc.bold("By layer"));
  lines.push(breakdownTable(summary.byLayer).toString());

  if (summary.missing.length > 0) {
    lines.push("");
    lines.push(pc.bold(pc.red(`Missing a required label (${summary.missing.length}):`)));
    for (const entry of summary.missing) {
      lines.push(`  - ${entry.testName} (missing: ${entry.missingLabels.join(", ")})`);
    }
  }

  return lines.join("\n");
}

function breakdownTable(counts: Record<string, number>): Table.Table {
  const table = new Table({ head: ["Value", "Tests"] });
  for (const [value, count] of Object.entries(counts).sort((a, b) => b[1] - a[1])) {
    table.push([value, String(count)]);
  }
  return table;
}

function colorPercentage(percentage: number): string {
  const text = `${percentage}%`;
  if (percentage >= 90) {
    return pc.green(text);
  }
  if (percentage >= 50) {
    return pc.yellow(text);
  }
  return pc.red(text);
}
