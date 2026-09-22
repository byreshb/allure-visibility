package io.github.byreshb.avl.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.byreshb.avl.junit5.fixture.CompliantFixture;
import io.github.byreshb.avl.junit5.fixture.FailEnforcementFixture;
import io.github.byreshb.avl.junit5.fixture.WarnEnforcementFixture;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Runs the sample fixtures under {@code io.github.byreshb.avl.junit5.fixture} through the JUnit
 * Platform {@code Launcher}, backed by the real {@code allure-junit5} adapter, then inspects the
 * Allure result JSON files it writes for them. This proves the extension's labels and policy
 * enforcement show up in Allure's own output, not only in a direct, in-process call.
 */
class AllureVisibilityExtensionIntegrationTest {

  private static final Path RESULTS_DIRECTORY = Paths.get("target", "allure-results");

  @BeforeAll
  static void clearPreviousResults() throws IOException {
    if (Files.isDirectory(RESULTS_DIRECTORY)) {
      try (Stream<Path> files = Files.list(RESULTS_DIRECTORY)) {
        for (Path file : files.toList()) {
          Files.deleteIfExists(file);
        }
      }
    }
  }

  @Test
  void compliantFixturePassesAndCarriesItsLabels() throws IOException {
    TestExecutionSummary summary = run(CompliantFixture.class);
    assertThat(summary.getTotalFailureCount()).isZero();

    String json = resultJsonFor(CompliantFixture.class, "appliesPromoCodeToTotal");
    assertThat(containsLabel(json, "layer", "API")).isTrue();
    assertThat(containsLabel(json, "team", "payments")).isTrue();
    assertThat(json).doesNotContain("avl.compliance");
  }

  @Test
  void warnFixturePassesButIsMarkedNonCompliant() throws IOException {
    TestExecutionSummary summary = run(WarnEnforcementFixture.class);
    assertThat(summary.getTotalFailureCount()).isZero();

    String json = resultJsonFor(WarnEnforcementFixture.class, "missingTeamStillPasses");
    assertThat(containsLabel(json, "layer", "API")).isTrue();
    assertThat(containsLabel(json, "avl.compliance", "warn:team")).isTrue();
  }

  @Test
  void failFixtureFailsTheTestNamingTheMissingLabel() throws IOException {
    TestExecutionSummary summary = run(FailEnforcementFixture.class);
    assertThat(summary.getTotalFailureCount()).isEqualTo(1);

    String json = resultJsonFor(FailEnforcementFixture.class, "missingTeamFailsTheTest");
    assertThat(json)
        .contains("\"status\":\"broken\"")
        .contains("AvlPolicyViolationException")
        .contains("is missing required label(s): team");
  }

  private TestExecutionSummary run(Class<?> testClass) {
    LauncherDiscoveryRequest request =
        LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(testClass))
            .build();
    Launcher launcher = LauncherFactory.create();
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(listener);
    launcher.execute(request);
    return listener.getSummary();
  }

  private String resultJsonFor(Class<?> testClass, String methodName) throws IOException {
    String fullName = testClass.getName() + "." + methodName;
    try (Stream<Path> files = Files.list(RESULTS_DIRECTORY)) {
      List<Path> matches =
          files
              .filter(path -> path.getFileName().toString().endsWith("-result.json"))
              .filter(path -> readSilently(path).contains(fullName))
              .toList();
      assertThat(matches).as("result file for %s", fullName).hasSize(1);
      return readSilently(matches.get(0));
    }
  }

  private static String readSilently(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static boolean containsLabel(String json, String name, String value) {
    Pattern pattern =
        Pattern.compile(
            "(\"name\":\""
                + Pattern.quote(name)
                + "\",\"value\":\""
                + Pattern.quote(value)
                + "\")|(\"value\":\""
                + Pattern.quote(value)
                + "\",\"name\":\""
                + Pattern.quote(name)
                + "\")");
    return pattern.matcher(json).find();
  }
}
