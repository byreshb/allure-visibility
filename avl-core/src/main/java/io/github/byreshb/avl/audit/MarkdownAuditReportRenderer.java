package io.github.byreshb.avl.audit;

/**
 * Renders an {@link AuditReport} as Markdown, formatted to paste straight into a PR description or
 * a team's quarterly quality review.
 */
public final class MarkdownAuditReportRenderer implements AuditReportRenderer {

  @Override
  public String render(AuditReport report) {
    StringBuilder md = new StringBuilder();
    md.append("# allure-visibility audit\n\n");
    md.append("- Total tests: ").append(report.totalTests()).append('\n');
    md.append("- Compliant: ").append(report.compliantCount()).append('\n');
    md.append("- Required labels: ")
        .append(String.join(", ", report.requiredLabels()))
        .append("\n\n");

    md.append("## Missing labels\n\n");
    md.append("| Label | Missing | Tests |\n");
    md.append("| --- | --- | --- |\n");
    report
        .missingByLabel()
        .forEach(
            (label, findings) -> {
              String tests =
                  findings.stream()
                      .map(TestFinding::displayName)
                      .reduce((a, b) -> a + "<br>" + b)
                      .orElse("");
              md.append("| `")
                  .append(label)
                  .append("` | ")
                  .append(findings.size())
                  .append(" | ")
                  .append(tests)
                  .append(" |\n");
            });

    md.append("\n## By file\n\n");
    md.append("| File | Tests | Compliant |\n");
    md.append("| --- | --- | --- |\n");
    report
        .byFile()
        .forEach(
            (file, findings) -> {
              long compliant = findings.stream().filter(TestFinding::isCompliant).count();
              md.append("| `")
                  .append(file)
                  .append("` | ")
                  .append(findings.size())
                  .append(" | ")
                  .append(compliant)
                  .append(" |\n");
            });

    return md.toString();
  }
}
