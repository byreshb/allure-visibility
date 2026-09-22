# JUnit 5

`AllureVisibilityExtension` (`io.github.byreshb.avl.junit5`) attaches labels to each test and
enforces `AvlConfig`'s policy for required ones.

## Usage

```java
@ExtendWith(AllureVisibilityExtension.class)
class CheckoutApiTest {

  @Layer(TestLayer.API)
  @Team("payments")
  @Test
  void appliesPromoCodeToTotal() { /* ... */ }
}
```

This is the primary, documented usage: put `@ExtendWith(AllureVisibilityExtension.class)` on a
base test class (or every test class) explicitly.

## What it does, on each test

1. Reflects over the test method and its declaring class for `@Layer`, `@Team`, `@Priority`,
   `@Component`, applying method-over-class precedence (see the main
   [README](../README.md#reference)).
2. Calls `io.qameta.allure.Allure.label(name, value)` for each label present, lower-cased
   (`layer`, `team`, `priority`, `component`).
3. Applies `AvlConfig`'s enforcement policy for whichever required labels are missing: under
   `warn`, attaches `avl.compliance=warn:<missing-labels>` and logs a warning, and the test still
   passes; under `fail`, throws `AvlPolicyViolationException`, naming exactly which required
   label was missing, which fails the test.

## Zero-code registration, with a caveat

`AllureVisibilityExtension` is also registered under
`META-INF/services/org.junit.jupiter.api.extension.Extension`, so it can be picked up by JUnit
5's own extension autodetection instead of an explicit `@ExtendWith`. That autodetection is
**opt-in**, not on by default:

```properties
# junit-platform.properties, on the test classpath
junit.jupiter.extensions.autodetection.enabled=true
```

With that property set, every test in the run gets the extension with no `@ExtendWith` needed at
all. Without it, `@ExtendWith(AllureVisibilityExtension.class)` (or `@RegisterExtension`) is
required, same as any other JUnit 5 extension. The accurate claim is "zero-code registration once
autodetection is enabled," not "automatic with no configuration at all."
