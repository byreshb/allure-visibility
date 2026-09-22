# Design

## Why four labels

Allure already ships `@Epic`/`@Feature`/`@Story`/`@Owner`/`@Severity`, and they cover a lot of
ground. What they don't give you is a way to say "every test must carry this label" or to find
out, without running the suite, which tests don't. allure-visibility adds four labels chosen to
answer the questions that come up once a suite crosses a few hundred tests across several teams:

- `layer` (`@Layer`, a `TestLayer`): what kind of test this is — `UNIT`, `INTEGRATION`, `API`,
  `UI`, `E2E`. Lets you ask "how much of our E2E surface actually has a documented owner?"
  separately from unit tests.
- `team` (`@Team`, free text): who owns this test. The direct answer to "which team owns this
  failing test."
- `priority` (`@Priority`, a `TestPriority`): how urgently a failure here needs attention,
  relative to everything else. See below for why this isn't the same thing as severity.
- `component` (`@Component`, free text): the product area under test, e.g. `checkout`,
  `billing-export`. Lets ownership and urgency questions be sliced by area, not just by team.

Four is a starting point, not a ceiling. More labels, more enforcement modes, or more renderers
can be added later as real needs come up.

## Priority is not severity

Allure's `@Severity` describes technical or functional impact: how badly the system breaks when
this fails (`BLOCKER`, `CRITICAL`, `NORMAL`, `MINOR`, `TRIVIAL`). `@Priority` here describes
business urgency: how soon the failure needs to be dealt with relative to everything else in the
backlog.

The two often agree, but not always. A cosmetic rendering bug on the flagship checkout page can
be low severity (nothing is broken) and high priority (it's visible to every paying customer
today). A crash in a rarely-used admin export tool can be high severity (a crash) and low
priority (three people use it, once a quarter). Collapsing the two into one axis loses that
distinction; allure-visibility keeps them separate on purpose.

## Two enforcement paths, and when each fits

`avl.enforcement` (see [configuration.md](configuration.md)) picks between two ways of reacting
to a test that's missing a required label:

- **`warn`**: the test still passes. An `avl.compliance=warn:<missing-labels>` label is attached
  and a warning is logged. Fits a suite that's mid-migration onto allure-visibility, or a team
  that wants visibility into gaps before turning enforcement into a hard gate.
- **`fail`**: the test fails with `AvlPolicyViolationException`, naming exactly which required
  label was missing on which test. Fits a suite where the required labels are already an
  established convention and a missing one is a real defect, not just a gap to track.

Both paths run at test-run time, through the JUnit 5 extension or the TestNG listener. A separate
static audit (`SuiteAuditor`, documented once it lands) answers the same coverage question
without running anything, which is the tool for a CI gate or a periodic report rather than a
per-test policy.
