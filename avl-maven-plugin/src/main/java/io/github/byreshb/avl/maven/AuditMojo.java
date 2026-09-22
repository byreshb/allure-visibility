package io.github.byreshb.avl.maven;

import io.github.byreshb.avl.audit.AuditReport;
import io.github.byreshb.avl.audit.AuditReportRenderer;
import io.github.byreshb.avl.audit.ConsoleAuditReportRenderer;
import io.github.byreshb.avl.audit.DefaultSuiteAuditor;
import io.github.byreshb.avl.audit.JsonAuditReportRenderer;
import io.github.byreshb.avl.audit.MarkdownAuditReportRenderer;
import io.github.byreshb.avl.audit.SuiteAuditor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Wraps {@link SuiteAuditor} as the {@code avl:audit} goal: audits {@code src/test/java} for
 * required label coverage and writes the report under {@code target/avl/}.
 *
 * <p>Bound to nothing by default; opt into a lifecycle phase (typically {@code verify}) per
 * project.
 */
@Mojo(name = "audit", requiresProject = true)
public final class AuditMojo extends AbstractMojo {

  private static final Set<String> KNOWN_LABELS = Set.of("team", "layer", "priority", "component");

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /**
   * Comma-separated required labels, a subset of {@code team}, {@code layer}, {@code priority},
   * {@code component}.
   */
  @Parameter(property = "avl.required.labels", defaultValue = "team,layer")
  private String requiredLabels = "team,layer";

  /** Fails the build if any test is missing a required label. */
  @Parameter(property = "avl.failOnMissing", defaultValue = "false")
  private boolean failOnMissing;

  /** Output format: {@code console}, {@code json} or {@code md}. */
  @Parameter(property = "avl.format", defaultValue = "console")
  private String format = "console";

  private final SuiteAuditor suiteAuditor;

  /** Creates the goal with the default {@link SuiteAuditor}. */
  public AuditMojo() {
    this(new DefaultSuiteAuditor());
  }

  AuditMojo(SuiteAuditor suiteAuditor) {
    this.suiteAuditor = suiteAuditor;
  }

  void setProject(MavenProject project) {
    this.project = project;
  }

  void setRequiredLabels(String requiredLabels) {
    this.requiredLabels = requiredLabels;
  }

  void setFailOnMissing(boolean failOnMissing) {
    this.failOnMissing = failOnMissing;
  }

  void setFormat(String format) {
    this.format = format;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Path sourceRoot = Path.of(project.getBasedir().getPath(), "src", "test", "java");
    if (!Files.isDirectory(sourceRoot)) {
      getLog().info("No test sources at " + sourceRoot + "; skipping audit");
      return;
    }

    Set<String> required = parseRequiredLabels(requiredLabels);
    AuditReport report = suiteAuditor.audit(sourceRoot, required);
    AuditReportRenderer renderer = rendererFor(format);
    String rendered = renderer.render(report);

    System.out.println(rendered);

    Path outputFile =
        Path.of(project.getBuild().getDirectory(), "avl", "audit." + extensionFor(format));
    writeReport(outputFile, rendered);

    long missing = report.totalTests() - report.compliantCount();
    getLog()
        .info(
            String.format(
                "%d/%d test(s) compliant; report written to %s",
                report.compliantCount(), report.totalTests(), outputFile));

    if (failOnMissing && missing > 0) {
      throw new MojoFailureException(
          missing
              + " of "
              + report.totalTests()
              + " test(s) are missing a required label; see "
              + outputFile);
    }
  }

  private Set<String> parseRequiredLabels(String rawValue) throws MojoExecutionException {
    Set<String> labels = new LinkedHashSet<>();
    for (String token : rawValue.split(",")) {
      String label = token.trim().toLowerCase(Locale.ROOT);
      if (label.isEmpty()) {
        continue;
      }
      if (!KNOWN_LABELS.contains(label)) {
        throw new MojoExecutionException(
            "Unknown label '"
                + label
                + "' in avl.required.labels; expected a subset of "
                + KNOWN_LABELS);
      }
      labels.add(label);
    }
    if (labels.isEmpty()) {
      throw new MojoExecutionException("avl.required.labels must not be empty");
    }
    return labels;
  }

  private AuditReportRenderer rendererFor(String requestedFormat) throws MojoExecutionException {
    return switch (requestedFormat.toLowerCase(Locale.ROOT)) {
      case "console" -> new ConsoleAuditReportRenderer();
      case "json" -> new JsonAuditReportRenderer();
      case "md" -> new MarkdownAuditReportRenderer();
      default ->
          throw new MojoExecutionException(
              "Unknown avl.format '" + requestedFormat + "'; expected console, json or md");
    };
  }

  private String extensionFor(String requestedFormat) {
    return switch (requestedFormat.toLowerCase(Locale.ROOT)) {
      case "json" -> "json";
      case "md" -> "md";
      default -> "txt";
    };
  }

  private void writeReport(Path outputFile, String content) throws MojoExecutionException {
    try {
      Files.createDirectories(outputFile.getParent());
      Files.writeString(outputFile, content);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write " + outputFile, e);
    }
  }
}
