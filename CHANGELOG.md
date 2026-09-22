# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Initial project scaffolding: parent POM, `avl-core` and `avl-maven-plugin` modules, CI and
  release workflows.
- `@Layer`, `@Team`, `@Priority`, `@Component` label annotations with method-over-class
  precedence, and `AvlConfig` for loading `avl.required.labels` / `avl.enforcement`.
- `AllureVisibilityExtension` (JUnit 5): attaches resolved labels to each test's Allure result
  and enforces `AvlConfig`'s warn/fail policy, proven against real Allure result JSON.

[Unreleased]: https://github.com/byreshb/allure-visibility/compare/main...HEAD
