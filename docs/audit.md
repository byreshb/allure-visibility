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

A standalone TypeScript CLI that reads the same report shape is planned; this page will cover it
alongside the Maven goal below once it lands.

## Maven goal: `avl:audit`

`avl-maven-plugin` wraps `SuiteAuditor` over `src/test/java` as the `avl:audit` goal. It is bound
to nothing by default — opt it into a phase (typically `verify`) per project:

```xml
<plugin>
  <groupId>io.github.byreshb</groupId>
  <artifactId>avl-maven-plugin</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <executions>
    <execution>
      <goals>
        <goal>audit</goal>
      </goals>
      <phase>verify</phase>
    </execution>
  </executions>
</plugin>
```

Parameters:

| Parameter               | Property              | Default    | Meaning |
| ------------------------ | ---------------------- | ---------- | ------- |
| `requiredLabels`         | `avl.required.labels`  | `team,layer` | same shape as `AvlConfig`'s key of the same name |
| `failOnMissing`          | `avl.failOnMissing`    | `false`    | fails the build the same way `avl.enforcement=fail` fails a single test |
| `format`                 | `avl.format`           | `console`  | `console`, `json` or `md` |

The rendered report is printed to stdout and always written under `target/avl/audit.<ext>`
(`.txt`, `.json` or `.md`), a real artifact usable in CI regardless of how the goal was invoked:

```bash
mvn avl:audit -Davl.format=md > target/avl/audit.md
```
