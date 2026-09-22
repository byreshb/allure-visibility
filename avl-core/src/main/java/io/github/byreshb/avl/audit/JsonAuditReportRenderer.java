package io.github.byreshb.avl.audit;

import java.util.List;
import java.util.Map;

/**
 * Renders an {@link AuditReport} as JSON, hand-written rather than through a JSON library, since
 * the report's shape is simple and fixed.
 */
public final class JsonAuditReportRenderer implements AuditReportRenderer {

  @Override
  public String render(AuditReport report) {
    StringBuilder json = new StringBuilder();
    json.append('{');
    json.append("\"totalTests\":").append(report.totalTests()).append(',');
    json.append("\"compliantCount\":").append(report.compliantCount()).append(',');
    json.append("\"requiredLabels\":").append(stringArray(report.requiredLabels())).append(',');
    json.append("\"findings\":").append(findingsJson(report.findings()));
    json.append('}');
    return json.toString();
  }

  private String findingsJson(List<TestFinding> findings) {
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < findings.size(); i++) {
      if (i > 0) {
        json.append(',');
      }
      json.append(findingJson(findings.get(i)));
    }
    return json.append(']').toString();
  }

  private String findingJson(TestFinding finding) {
    StringBuilder json = new StringBuilder();
    json.append('{');
    json.append("\"className\":").append(quote(finding.className())).append(',');
    json.append("\"methodName\":").append(quote(finding.methodName())).append(',');
    json.append("\"sourceFile\":").append(quote(finding.sourceFile().toString())).append(',');
    json.append("\"resolvedLabels\":").append(stringMap(finding.resolvedLabels())).append(',');
    json.append("\"missingRequiredLabels\":").append(stringArray(finding.missingRequiredLabels()));
    json.append('}');
    return json.toString();
  }

  private String stringArray(Iterable<String> values) {
    StringBuilder json = new StringBuilder("[");
    boolean first = true;
    for (String value : values) {
      if (!first) {
        json.append(',');
      }
      json.append(quote(value));
      first = false;
    }
    return json.append(']').toString();
  }

  private String stringMap(Map<String, String> values) {
    StringBuilder json = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, String> entry : values.entrySet()) {
      if (!first) {
        json.append(',');
      }
      json.append(quote(entry.getKey())).append(':').append(quote(entry.getValue()));
      first = false;
    }
    return json.append('}').toString();
  }

  private String quote(String value) {
    StringBuilder out = new StringBuilder("\"");
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"' -> out.append("\\\"");
        case '\\' -> out.append("\\\\");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        default -> out.append(c);
      }
    }
    return out.append('"').toString();
  }
}
