# TestNG

`AllureVisibilityListener` (`io.github.byreshb.avl.testng`) does the same job as the JUnit 5
extension: attaches labels to each test and enforces `AvlConfig`'s policy for required ones. The
label resolution and policy logic are identical; only the integration point differs.

## Usage

Explicit registration, the same shape as the JUnit 5 module:

```java
@Listeners(AllureVisibilityListener.class)
public class CheckoutApiTest {

  @Layer(TestLayer.API)
  @Team("payments")
  @Test
  public void appliesPromoCodeToTotal() { /* ... */ }
}
```

## The real difference from JUnit 5: `ServiceLoader` autodetection is not opt-in

`AllureVisibilityListener` is registered under
`META-INF/services/org.testng.ITestNGListener`. TestNG loads every listener declared this way
through the standard Java `ServiceLoader` mechanism **automatically, with no configuration flag
to turn on**. This is a real, checkable difference from the JUnit 5 module documented in
[docs/junit5.md](junit5.md):

- JUnit 5's own extension autodetection (which the JUnit 5 module also registers for, under
  `META-INF/services/org.junit.jupiter.api.extension.Extension`) is **opt-in**:
  `junit.jupiter.extensions.autodetection.enabled=true` must be set in
  `junit-platform.properties` before the JUnit Platform will look at that file at all. Without
  it, `@ExtendWith(AllureVisibilityExtension.class)` (or `@RegisterExtension`) is required on
  every test class.
- TestNG's `ServiceLoader`-based listener loading has no equivalent flag. The moment
  `avl-core` is on the test classpath, `AllureVisibilityListener` is active for every TestNG test
  in the run, `@Listeners` or not.

Practically: adding `avl-core` as a dependency to a JUnit 5 project changes nothing on its own —
`@ExtendWith` (or turning on autodetection) is still required. Adding it to a TestNG project
turns on label attachment and enforcement immediately, for every test, whether or not any test
class mentions `AllureVisibilityListener`. `@Listeners` is still useful for readability and for
IDEs that resolve test configuration statically, but it is not what makes the listener run.

If that automatic behavior isn't wanted for a particular suite, the listener can't be selectively
disabled per class through TestNG's own APIs; the practical way to fully opt out is not adding
`avl-core` to that suite's test classpath at all.

## Why `IInvokedMethodListener`, not `ITestListener`

`AllureVisibilityListener` implements `IInvokedMethodListener` and does its work in
`beforeInvocation`, not the more obvious `ITestListener#onTestStart`. This is deliberate:
`allure-testng`'s own listener starts the underlying Allure lifecycle test case from its
`onTestStart`, and TestNG gives no ordering guarantee between two independently
`ServiceLoader`-discovered `ITestListener`s. If ours ran first, every `Allure.label(...)` call
would silently no-op against a lifecycle with no test case open yet. `beforeInvocation` is
guaranteed to run only after every `onTestStart` notification — Allure's included — has already
completed, so the label calls always land.
