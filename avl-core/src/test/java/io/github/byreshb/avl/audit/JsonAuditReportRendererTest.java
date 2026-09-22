package io.github.byreshb.avl.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JsonAuditReportRendererTest {

  private final JsonAuditReportRenderer renderer = new JsonAuditReportRenderer();

  @Test
  void rendersValidJsonWithFindingsAndEscaping() {
    TestFinding finding =
        new TestFinding(
            "fixtures.MissingLabelsTest",
            "missingTeamOnly",
            Path.of("fixtures/MissingLabelsTest.java"),
            Map.of("layer", "UNIT"),
            Set.of("team"));
    AuditReport report = new AuditReport(1, Set.of("team", "layer"), List.of(finding));

    String json = renderer.render(report);

    assertThat(json).contains("\"totalTests\":1");
    assertThat(json).contains("\"compliantCount\":0");
    assertThat(json).contains("\"className\":\"fixtures.MissingLabelsTest\"");
    assertThat(json).contains("\"methodName\":\"missingTeamOnly\"");
    assertThat(json).contains("\"layer\":\"UNIT\"");
    assertThat(json).contains("\"missingRequiredLabels\":[\"team\"]");
  }

  @Test
  void escapesQuotesAndBackslashesInValues() {
    TestFinding finding =
        new TestFinding(
            "fixtures.WeirdTest",
            "quote\"andBackslash\\Test",
            Path.of("fixtures/WeirdTest.java"),
            Map.of(),
            Set.of());
    AuditReport report = new AuditReport(1, Set.of(), List.of(finding));

    String json = renderer.render(report);

    assertThat(json).contains("\"methodName\":\"quote\\\"andBackslash\\\\Test\"");
  }
}
