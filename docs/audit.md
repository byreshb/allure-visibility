# Static audit

`SuiteAuditor` (`io.github.byreshb.avl.audit`) answers the coverage question — "how many of our
tests actually have a documented owner?" — without running the suite at all. It parses every
`.java` file under a given source root with `javaparser`, finds test methods (`@org.junit.jupiter.api.Test`
and `@org.testng.annotations.Test`), applies the same method-over-class label precedence as the
runtime extension and listener, and produces an `AuditReport`.

## Usage

```java
SuiteAuditor auditor = new DefaultSuiteAuditor();
AuditReport report = auditor.audit(Path.of("src/test/java"), Set.of("team", "layer"));

report.totalTests();       // tests found
report.compliantCount();   // tests carrying every required label
report.missingByLabel();   // required label -> the tests missing it
report.byFile();           // source file -> the tests found in it
```

## Renderers

Three `AuditReportRenderer` implementations turn a report into text:

- `ConsoleAuditReportRenderer`: a plain-text summary.
- `JsonAuditReportRenderer`: JSON, hand-written (no JSON library dependency) since the report's
  shape is simple and fixed.
- `MarkdownAuditReportRenderer`: Markdown, formatted to paste straight into a PR description or a
  team's quarterly quality review.

```java
String markdown = new MarkdownAuditReportRenderer().render(report);
```

A Maven goal (`avl:audit`) and a standalone TypeScript CLI that read the same report shape are
planned; this page will cover them side by side once they land.
