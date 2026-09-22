package io.github.byreshb.avl.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuditMojoTest {

  @Test
  void writesJsonReportAndFailsWhenFailOnMissingIsSet(@TempDir Path tempDir) throws IOException {
    writeFixtureSource(tempDir);
    AuditMojo mojo = mojoFor(tempDir);
    mojo.setFormat("json");
    mojo.setFailOnMissing(true);

    assertThatThrownBy(mojo::execute)
        .isInstanceOf(MojoFailureException.class)
        .hasMessageContaining("1 of 1")
        .hasMessageContaining("missing a required label");

    Path outputFile = tempDir.resolve("target/avl/audit.json");
    assertThat(outputFile).exists();
    assertThat(Files.readString(outputFile))
        .contains("\"totalTests\":1")
        .contains("\"methodName\":\"missingTeam\"");
  }

  @Test
  void doesNotFailByDefaultEvenWithMissingLabels(@TempDir Path tempDir) throws IOException {
    writeFixtureSource(tempDir);
    AuditMojo mojo = mojoFor(tempDir);
    mojo.setFormat("console");

    assertThatCode(mojo::execute).doesNotThrowAnyException();

    assertThat(tempDir.resolve("target/avl/audit.txt")).exists();
  }

  @Test
  void writesMarkdownReport(@TempDir Path tempDir)
      throws IOException, MojoExecutionException, MojoFailureException {
    writeFixtureSource(tempDir);
    AuditMojo mojo = mojoFor(tempDir);
    mojo.setFormat("md");

    mojo.execute();

    Path outputFile = tempDir.resolve("target/avl/audit.md");
    assertThat(outputFile).exists();
    assertThat(Files.readString(outputFile)).contains("# allure-visibility audit");
  }

  @Test
  void rejectsAnUnknownFormat(@TempDir Path tempDir) throws IOException {
    writeFixtureSource(tempDir);
    AuditMojo mojo = mojoFor(tempDir);
    mojo.setFormat("yaml");

    assertThatThrownBy(mojo::execute)
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining("yaml");
  }

  @Test
  void skipsSilentlyWhenNoTestSourcesExist(@TempDir Path tempDir) {
    AuditMojo mojo = mojoFor(tempDir);

    assertThatCode(mojo::execute).doesNotThrowAnyException();
    assertThat(tempDir.resolve("target/avl")).doesNotExist();
  }

  private void writeFixtureSource(Path tempDir) throws IOException {
    Path sourceDir = tempDir.resolve("src/test/java/sample");
    Files.createDirectories(sourceDir);
    Files.writeString(
        sourceDir.resolve("MissingTeamTest.java"),
        """
        package sample;

        import org.junit.jupiter.api.Test;

        public class MissingTeamTest {

          @Test
          public void missingTeam() {}
        }
        """);
  }

  private AuditMojo mojoFor(Path tempDir) {
    Model model = new Model();
    Build build = new Build();
    build.setDirectory(tempDir.resolve("target").toString());
    model.setBuild(build);
    MavenProject project = new MavenProject(model);
    project.setFile(tempDir.resolve("pom.xml").toFile());

    AuditMojo mojo = new AuditMojo();
    mojo.setProject(project);
    return mojo;
  }
}
