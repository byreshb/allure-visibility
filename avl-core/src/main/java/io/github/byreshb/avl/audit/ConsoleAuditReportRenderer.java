package io.github.byreshb.avl.audit;

/** Renders an {@link AuditReport} as a plain-text console summary. */
public final class ConsoleAuditReportRenderer implements AuditReportRenderer {

  @Override
  public String render(AuditReport report) {
    StringBuilder out = new StringBuilder();
    out.append("allure-visibility audit\n");
    out.append("========================\n");
    out.append("Total tests: ").append(report.totalTests()).append('\n');
    out.append("Compliant: ").append(report.compliantCount()).append('\n');
    out.append("Required labels: ")
        .append(String.join(", ", report.requiredLabels()))
        .append("\n\n");

    report
        .missingByLabel()
        .forEach(
            (label, findings) -> {
              out.append("Missing \"")
                  .append(label)
                  .append("\": ")
                  .append(findings.size())
                  .append('\n');
              for (TestFinding finding : findings) {
                out.append("  - ")
                    .append(finding.displayName())
                    .append(" (")
                    .append(finding.sourceFile())
                    .append(")\n");
              }
            });

    out.append("\nBy file:\n");
    report
        .byFile()
        .forEach(
            (file, findings) -> {
              long compliant = findings.stream().filter(TestFinding::isCompliant).count();
              out.append("  ")
                  .append(file)
                  .append(": ")
                  .append(findings.size())
                  .append(" tests, ")
                  .append(compliant)
                  .append(" compliant\n");
            });

    return out.toString();
  }
}
