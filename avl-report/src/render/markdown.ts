import { REQUIRED_LABEL_NAMES, type ComplianceSummary } from "../types.js";

/**
 * Renders a {@link ComplianceSummary} as Markdown, formatted to paste straight into a PR
 * description or a team's quarterly quality review.
 */
export function renderMarkdown(summary: ComplianceSummary): string {
  const lines: string[] = [];
  lines.push("# allure-visibility report");
  lines.push("");
  lines.push(`- Total tests: ${summary.totalTests}`);
  lines.push(`- Required labels: ${summary.requiredLabels.join(", ")}`);
  lines.push("");

  lines.push("## Coverage", "", "| Label | Count | Coverage |", "| --- | --- | --- |");
  for (const label of REQUIRED_LABEL_NAMES) {
    const { count, percentage } = summary.coverage[label];
    lines.push(`| \`${label}\` | ${count} | ${percentage}% |`);
  }

  lines.push("", "## By team", "", ...breakdownRows(summary.byTeam));
  lines.push("", "## By layer", "", ...breakdownRows(summary.byLayer));

  if (summary.missing.length > 0) {
    lines.push("", `## Missing a required label (${summary.missing.length})`, "");
    for (const entry of summary.missing) {
      lines.push(`- \`${entry.testName}\` (missing: ${entry.missingLabels.join(", ")})`);
    }
  }

  return lines.join("\n");
}

function breakdownRows(counts: Record<string, number>): string[] {
  const rows = ["| Value | Tests |", "| --- | --- |"];
  for (const [value, count] of Object.entries(counts).sort((a, b) => b[1] - a[1])) {
    rows.push(`| ${value} | ${count} |`);
  }
  return rows;
}
