package io.github.byreshb.avl.audit;

/** Renders an {@link AuditReport} as text in a particular format. */
public interface AuditReportRenderer {

  /**
   * Renders the report.
   *
   * @param report the report to render
   * @return the rendered text
   */
  String render(AuditReport report);
}
