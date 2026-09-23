# allure-visibility-report

`avl-core`'s JUnit 5 extension and TestNG listener attach `layer`/`team`/`priority`/`component`
labels to Allure results, but the standard Allure report doesn't answer "what fraction of our
tests actually carry a documented owner?" — you'd have to open every result and count.
`allure-visibility-report` reads the Allure results a run already produced and prints that answer
directly: total tests, the percentage carrying each label, a breakdown by team and by layer, and
the specific tests missing a required one.

This is a standalone compliance report for this project's own labels. It doesn't integrate with
any other test-health tool or dashboard; feeding its output into a broader dashboard is a natural
future extension, not something it does today.

## Install

Not published to npm yet (planned, see
[docs/releasing.md](../docs/releasing.md#avl-report-npm)). Until then, run it from a checkout:

```bash
cd avl-report
npm install
npm run build
node dist/cli.js <allure-results-dir>
```

## Usage

```bash
npx allure-visibility-report <allure-results-dir>
npx allure-visibility-report <allure-results-dir> --format json
npx allure-visibility-report <allure-results-dir> --format md
npx allure-visibility-report <allure-results-dir> --config path/to/allure.properties
```

`--config` points at an `allure.properties` file and reads its `avl.required.labels` key, the
same key and shape `AvlConfig` reads on the Java side — one source of truth for what "required"
means, not two. Without `--config` (or when the file has no `avl.required.labels` key), the
default is `team,layer`, matching `AvlConfig`'s own default.

## Reference

| Flag       | Default   | Meaning                                                                |
| ---------- | --------- | ---------------------------------------------------------------------- |
| `--format` | `console` | `console` (colored table), `json` or `md`                              |
| `--config` | none      | path to an `allure.properties` file to read `avl.required.labels` from |

The report:

- `totalTests`: the number of `*-result.json` files found.
- `coverage`: for each of `team`/`layer`/`priority`/`component`, how many tests carry it and what
  percentage that is of the total.
- `byTeam` / `byLayer`: counts grouped by the `team` and `layer` label values actually present.
- `missing`: the tests missing at least one required label, and which ones.

## Building and testing

```bash
npm run build   # compile to dist/
npm test        # run the test suite with coverage
npm run lint    # eslint
npm run format  # apply Prettier
npm run check   # tsc --noEmit && eslint . && prettier --check . (what CI runs)
```

Tests run against real sample Allure result JSON checked into [`fixtures/`](fixtures), not
synthetic mocks of this package's own parsing code.
