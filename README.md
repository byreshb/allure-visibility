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

Early scaffolding. This README and the modules below will fill in as the delivery plan
progresses; the annotations, extensions and audit tooling described above aren't implemented yet.

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
