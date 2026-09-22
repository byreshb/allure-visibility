# Configuration

`AvlConfig` is loaded once per JVM (`AvlConfig.getInstance()`). Every key is prefixed `avl.` so it
sits safely alongside Allure's own `allure.*` keys in the same `allure.properties` file. Each key
is resolved in order, first found wins:

1. A system property (`-Davl.required.labels=...`).
2. `allure.properties` on the classpath — read independently of Allure's own loading of that
   file, since Allure's internal loader class is not public API.
3. A built-in default.

## Keys

### `avl.required.labels`

A comma-separated subset of `team`, `layer`, `priority`, `component` (case-insensitive,
whitespace around entries is trimmed). These are the labels a test must carry for the enforcement
policy in `avl.enforcement` to consider it compliant.

- Default: `team,layer`
- An unknown label name, or a value that resolves to no labels at all, fails config loading with
  a message naming the problem.

### `avl.enforcement`

`warn` or `fail` (case-insensitive), see [design.md](design.md#two-enforcement-paths-and-when-each-fits)
for what each one does.

- Default: `warn`
- Any other value fails config loading with a message naming the problem.

## Example

```properties
# allure.properties
allure.results.directory=target/allure-results
avl.required.labels=team,layer
avl.enforcement=fail
```
