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

## Maven goal: `avl:audit`

`avl-maven-plugin` wraps `SuiteAuditor` over `src/test/java` as the `avl:audit` goal. It is bound
to nothing by default — opt it into a phase (typically `verify`) per project:

```xml
<plugin>
  <groupId>io.github.byreshb</groupId>
  <artifactId>avl-maven-plugin</artifactId>
  <version>1.0.0</version>
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

## TypeScript CLI: `allure-visibility-report`

`avl-maven-plugin` and `SuiteAuditor` answer the coverage question statically, from source, before
anything runs. `avl-report`'s `allure-visibility-report` CLI answers the same shape of question
the other way around: it reads the Allure results a run already produced (the standard
`*-result.json` files) and reports on the labels that actually made it into the report, rather
than on what the source declares.

```bash
npx allure-visibility-report target/allure-results
npx allure-visibility-report target/allure-results --format md --config allure.properties
```

Both readers agree on what "required" means: `allure-visibility-report`'s `--config` flag points
at the same `allure.properties` file and reads the same `avl.required.labels` key `AvlConfig`
reads on the Java side, and falls back to the same `team,layer` default when it isn't set. See
[avl-report/README.md](../avl-report/README.md) for the full flag reference.

| | `SuiteAuditor` / `avl:audit` | `allure-visibility-report` |
| --- | --- | --- |
| Source of truth | test source files | Allure result JSON |
| When it runs | before (or without) running tests | after a run has produced results |
| Output | console / JSON / Markdown | console (colored) / JSON / Markdown |
| Finds | tests missing a label, by file | tests missing a label, by team/layer breakdown |
