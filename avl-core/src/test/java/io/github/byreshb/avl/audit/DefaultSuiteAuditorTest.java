package io.github.byreshb.avl.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DefaultSuiteAuditorTest {

  private final SuiteAuditor auditor = new DefaultSuiteAuditor();

  @Test
  void auditsAFixtureSourceTreeWithKnownMissingLabels() throws URISyntaxException {
    AuditReport report = auditor.audit(fixturesRoot(), Set.of("team", "layer"));

    assertThat(report.totalTests()).isEqualTo(5);
    assertThat(report.compliantCount()).isEqualTo(2);
    assertThat(report.missingByLabel().get("team")).hasSize(2);
    assertThat(report.missingByLabel().get("layer")).hasSize(1);
    assertThat(report.findings())
        .extracting(TestFinding::methodName)
        .containsExactlyInAnyOrder(
            "appliesPromoCodeToTotal",
            "methodOverridesClassTeam",
            "missingTeamOnly",
            "alsoMissingTeam",
            "missingLayerOnly");
  }

  @Test
  void ignoresClassesWithNoTestMethods() throws URISyntaxException {
    AuditReport report = auditor.audit(fixturesRoot(), Set.of("team", "layer"));

    assertThat(report.findings())
        .extracting(TestFinding::className)
        .doesNotContain("fixtures.NotATestClass");
  }

  @Test
  void resolvesMethodLevelLabelOverClassLevel() throws URISyntaxException {
    AuditReport report = auditor.audit(fixturesRoot(), Set.of("team", "layer"));

    TestFinding finding = findingFor(report, "methodOverridesClassTeam");

    assertThat(finding.resolvedLabels()).containsEntry("team", "checkout");
    assertThat(finding.resolvedLabels()).containsEntry("layer", "API");
    assertThat(finding.isCompliant()).isTrue();
  }

  @Test
  void detectsTestNgTestMethodsToo() throws URISyntaxException {
    AuditReport report = auditor.audit(fixturesRoot(), Set.of("team", "layer"));

    TestFinding finding = findingFor(report, "missingLayerOnly");

    assertThat(finding.className()).isEqualTo("fixtures.TestNgSampleTest");
    assertThat(finding.resolvedLabels()).containsEntry("team", "platform");
    assertThat(finding.missingRequiredLabels()).containsExactly("layer");
  }

  private TestFinding findingFor(AuditReport report, String methodName) {
    List<TestFinding> matches =
        report.findings().stream().filter(f -> f.methodName().equals(methodName)).toList();
    assertThat(matches).as("finding for %s", methodName).hasSize(1);
    return matches.get(0);
  }

  private Path fixturesRoot() throws URISyntaxException {
    URL resource = getClass().getClassLoader().getResource("audit-fixtures");
    assertThat(resource).as("audit-fixtures resource directory").isNotNull();
    return Path.of(resource.toURI());
  }
}
