# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-09-22

### Added

- Initial project scaffolding: parent POM, `avl-core` and `avl-maven-plugin` modules, CI and
  release workflows.
- `@Layer`, `@Team`, `@Priority`, `@Component` label annotations with method-over-class
  precedence, and `AvlConfig` for loading `avl.required.labels` / `avl.enforcement`.
- `AllureVisibilityExtension` (JUnit 5): attaches resolved labels to each test's Allure result
  and enforces `AvlConfig`'s warn/fail policy, proven against real Allure result JSON.
- `AllureVisibilityListener` (TestNG): same label attachment and enforcement, registered via
  `ServiceLoader` autodetection (no opt-in flag, unlike the JUnit 5 module), proven against real
  Allure result JSON.
- `SuiteAuditor`: static audit of a test source tree for required label coverage, with
  console/JSON/Markdown renderers, tested against a fixture set of test files with known-missing
  labels.
- `avl-maven-plugin`: `avl:audit` goal wrapping `SuiteAuditor` over `src/test/java`, with
  `failOnMissing` and `format` parameters, writing a real report under `target/avl/`.

[Unreleased]: https://github.com/byreshb/allure-visibility/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/byreshb/allure-visibility/releases/tag/v1.0.0
