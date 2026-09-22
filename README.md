# allure-visibility

[![CI](https://github.com/byreshb/allure-visibility/actions/workflows/ci.yml/badge.svg)](https://github.com/byreshb/allure-visibility/actions/workflows/ci.yml)

Allure already lets a test carry `@Epic`/`@Feature`/`@Story`/`@Owner`/`@Severity`, but nothing
stops half a suite from having them and half from not: there's no way to require a label, and no
way to find out which tests are missing one without opening the report and looking. As a suite
grows past a few hundred tests across several teams, "which team owns this failing test" and "how
many of our API tests actually have a documented owner" become real, recurring questions with no
good answer.

allure-visibility adds a small set of purpose-built labels (layer, team, priority, component,
distinct from Allure's own severity, which is technical impact rather than business priority or
ownership), a runtime extension that attaches them automatically, and an enforcement policy, warn
or fail, for tests that skip a required one, plus a static audit that answers the coverage
question without running the suite at all.

## Status

The four label annotations, `AvlConfig`, the JUnit 5 extension, the TestNG listener and the
static audit (`SuiteAuditor`) are implemented in `avl-core`. The Maven plugin described above
isn't implemented yet; this README will fill in as the delivery plan progresses.

## Install

Not on Maven Central yet (planned, see [docs/releasing.md](docs/releasing.md)). Until then, build
and install locally from a checkout:

```bash
mvn install
```

```xml
<dependency>
  <groupId>io.github.byreshb</groupId>
  <artifactId>avl-core</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick start

```java
@ExtendWith(AllureVisibilityExtension.class)
class CheckoutApiTest {

  @Layer(TestLayer.API)
  @Team("payments")
  @Priority(TestPriority.P1)
  @Component("checkout")
  @Test
  void appliesPromoCodeToTotal() { /* ... */ }
}
```

```properties
# allure.properties
allure.results.directory=target/allure-results
avl.required.labels=team,layer
avl.enforcement=fail
```

See [docs/junit5.md](docs/junit5.md) for what the extension does on each test.

TestNG uses the same annotations and the same `AvlConfig` policy, through
`AllureVisibilityListener`:

```java
@Listeners(AllureVisibilityListener.class)
public class CheckoutApiTest {

  @Layer(TestLayer.API)
  @Team("payments")
  @Test
  public void appliesPromoCodeToTotal() { /* ... */ }
}
```

See [docs/testng.md](docs/testng.md), including a real, checkable difference from JUnit 5: TestNG
loads `ServiceLoader`-declared listeners automatically, with no opt-in flag.

## Reference

Four annotations, each usable on a test class, a test method, or both — a method-level
annotation overrides the same annotation at class level, which is the default for a method that
declares none:

| Annotation     | Value               | Attached label |
| -------------- | -------------------- | --------------- |
| `@Layer`       | `TestLayer` (`UNIT`, `INTEGRATION`, `API`, `UI`, `E2E`) | `layer` |
| `@Team`        | free text, non-blank | `team` |
| `@Priority`    | `TestPriority` (`P0`–`P3`) | `priority` |
| `@Component`   | free text, non-blank | `component` |

`@Priority` is deliberately distinct from Allure's own `@Severity`: severity is technical impact,
priority is business urgency. See [docs/design.md](docs/design.md) for why.

Configuration (`avl.required.labels`, `avl.enforcement`) is documented in
[docs/configuration.md](docs/configuration.md).

`SuiteAuditor` answers the coverage question statically, without running the suite:

```java
SuiteAuditor auditor = new DefaultSuiteAuditor();
AuditReport report = auditor.audit(Path.of("src/test/java"), Set.of("team", "layer"));
String markdown = new MarkdownAuditReportRenderer().render(report);
```

See [docs/audit.md](docs/audit.md).

## Building and testing

```bash
mvn test               # run the test suite
mvn verify              # run tests, enforce coverage, build artifacts
mvn spotless:check      # verify formatting without changing files
mvn javadoc:javadoc     # generate Javadoc
```

## Continuous integration

Every push and pull request to `main` runs `mvn spotless:check` and `mvn verify` via GitHub
Actions (see the badge above). Surefire reports are uploaded as build artifacts on every run.

## Releasing

See [docs/releasing.md](docs/releasing.md).

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
