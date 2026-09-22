package io.github.byreshb.avl.testng;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.byreshb.avl.annotation.DefaultLabelResolver;
import io.github.byreshb.avl.config.AvlConfig;
import io.github.byreshb.avl.config.DefaultLabelEnforcer;
import io.github.byreshb.avl.testng.fixture.CompliantFixture;
import io.github.byreshb.avl.testng.fixture.FailEnforcementFixture;
import io.github.byreshb.avl.testng.fixture.WarnEnforcementFixture;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

/**
 * Runs the sample fixtures under {@code io.github.byreshb.avl.testng.fixture} through {@code
 * org.testng.TestNG}, backed by the real {@code allure-testng} adapter, then inspects the Allure
 * result JSON files it writes for them. This proves the listener's labels and policy enforcement
 * show up in Allure's own output, not only in a direct, in-process call, and that {@link
 * CompliantFixture} picks up {@link AllureVisibilityListener} with no listener declared anywhere,
 * through TestNG's {@code ServiceLoader}-based auto-discovery.
 */
class AllureVisibilityListenerIntegrationTest {

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
  void compliantFixturePassesAndCarriesItsLabelsViaServiceLoader() throws IOException {
    TestListenerAdapter result = run(CompliantFixture.class);
    assertThat(result.getFailedTests()).isEmpty();

    String json = resultJsonFor(CompliantFixture.class, "appliesPromoCodeToTotal");
    assertThat(containsLabel(json, "layer", "API")).isTrue();
    assertThat(containsLabel(json, "team", "payments")).isTrue();
  }

  @Test
  void warnFixturePassesButIsMarkedNonCompliant() throws IOException {
    TestListenerAdapter result =
        run(WarnEnforcementFixture.class, listenerFor("team,layer", "warn"));
    assertThat(result.getFailedTests()).isEmpty();

    String json = resultJsonFor(WarnEnforcementFixture.class, "missingTeamStillPasses");
    assertThat(containsLabel(json, "avl.compliance", "warn:team")).isTrue();
  }

  @Test
  void failFixtureFailsTheTestNamingTheMissingLabel() throws IOException {
    TestListenerAdapter result =
        run(FailEnforcementFixture.class, listenerFor("team,layer", "fail"));
    assertThat(result.getFailedTests()).hasSize(1);

    String json = resultJsonFor(FailEnforcementFixture.class, "missingTeamFailsTheTest");
    assertThat(json)
        .contains("AvlPolicyViolationException")
        .contains("is missing required label(s): team");
  }

  private TestListenerAdapter run(Class<?> testClass, ITestNGListener... extraListeners) {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class<?>[] {testClass});
    testng.setUseDefaultListeners(false);
    TestListenerAdapter adapter = new TestListenerAdapter();
    testng.addListener(adapter);
    for (ITestNGListener listener : extraListeners) {
      testng.addListener(listener);
    }
    testng.run();
    return adapter;
  }

  private AllureVisibilityListener listenerFor(String requiredLabels, String enforcement) {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.required.labels", requiredLabels);
    classpathProperties.setProperty("avl.enforcement", enforcement);
    AvlConfig config = AvlConfig.load(new Properties(), classpathProperties);
    return new AllureVisibilityListener(
        new DefaultLabelResolver(), new DefaultLabelEnforcer(config));
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
