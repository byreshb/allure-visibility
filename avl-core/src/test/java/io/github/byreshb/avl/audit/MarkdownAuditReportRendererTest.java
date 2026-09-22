package io.github.byreshb.avl.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MarkdownAuditReportRendererTest {

  private final MarkdownAuditReportRenderer renderer = new MarkdownAuditReportRenderer();

  @Test
  void rendersHeadingsTablesAndMissingLabelRows() {
    TestFinding finding =
        new TestFinding(
            "fixtures.MissingLabelsTest",
            "missingTeamOnly",
            Path.of("fixtures/MissingLabelsTest.java"),
            Map.of("layer", "UNIT"),
            Set.of("team"));
    AuditReport report = new AuditReport(1, Set.of("team", "layer"), List.of(finding));

    String markdown = renderer.render(report);

    assertThat(markdown)
        .contains("# allure-visibility audit")
        .contains("- Total tests: 1")
        .contains("| Label | Missing | Tests |")
        .contains("| `team` | 1 | fixtures.MissingLabelsTest#missingTeamOnly |")
        .contains("| `layer` | 0 |")
        .contains("## By file")
        .contains("| `fixtures/MissingLabelsTest.java` | 1 | 0 |");
  }
}
