package io.github.byreshb.avl.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConsoleAuditReportRendererTest {

  private final ConsoleAuditReportRenderer renderer = new ConsoleAuditReportRenderer();

  @Test
  void rendersTotalsAndMissingLabelsAndFileBreakdown() {
    TestFinding compliant =
        new TestFinding(
            "fixtures.CompliantTest",
            "appliesPromoCodeToTotal",
            Path.of("fixtures/CompliantTest.java"),
            Map.of("team", "payments", "layer", "API"),
            Set.of());
    TestFinding missingTeam =
        new TestFinding(
            "fixtures.MissingLabelsTest",
            "missingTeamOnly",
            Path.of("fixtures/MissingLabelsTest.java"),
            Map.of("layer", "UNIT"),
            Set.of("team"));
    AuditReport report =
        new AuditReport(2, Set.of("team", "layer"), List.of(compliant, missingTeam));

    String output = renderer.render(report);

    assertThat(output)
        .contains("Total tests: 2")
        .contains("Compliant: 1")
        .contains("Missing \"team\": 1")
        .contains("fixtures.MissingLabelsTest#missingTeamOnly")
        .contains("fixtures/CompliantTest.java")
        .contains("fixtures/MissingLabelsTest.java: 1 tests, 0 compliant");
  }
}
