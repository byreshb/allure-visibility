package io.github.byreshb.avl.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.byreshb.avl.config.AvlConfig.Enforcement;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AvlConfigTest {

  @AfterEach
  void clearSystemProperties() {
    System.clearProperty("avl.required.labels");
    System.clearProperty("avl.enforcement");
  }

  @Test
  void defaultsToTeamAndLayerWarnWhenNothingIsConfigured() {
    AvlConfig config = AvlConfig.load(new Properties(), new Properties());

    assertThat(config.requiredLabels()).isEqualTo(Set.of("team", "layer"));
    assertThat(config.enforcement()).isEqualTo(Enforcement.WARN);
  }

  @Test
  void readsRequiredLabelsFromClasspathProperties() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.required.labels", "priority, component");

    AvlConfig config = AvlConfig.load(new Properties(), classpathProperties);

    assertThat(config.requiredLabels()).isEqualTo(Set.of("priority", "component"));
  }

  @Test
  void readsEnforcementFromClasspathPropertiesCaseInsensitively() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.enforcement", "FAIL");

    AvlConfig config = AvlConfig.load(new Properties(), classpathProperties);

    assertThat(config.enforcement()).isEqualTo(Enforcement.FAIL);
  }

  @Test
  void systemPropertyTakesPrecedenceOverClasspathProperty() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("avl.enforcement", "fail");
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.enforcement", "warn");

    AvlConfig config = AvlConfig.load(systemProperties, classpathProperties);

    assertThat(config.enforcement()).isEqualTo(Enforcement.FAIL);
  }

  @Test
  void rejectsUnknownRequiredLabel() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.required.labels", "team,severity");

    assertThatThrownBy(() -> AvlConfig.load(new Properties(), classpathProperties))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("severity");
  }

  @Test
  void rejectsBlankRequiredLabels() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.required.labels", " , ,");

    assertThatThrownBy(() -> AvlConfig.load(new Properties(), classpathProperties))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("avl.required.labels");
  }

  @Test
  void rejectsUnknownEnforcementValue() {
    Properties classpathProperties = new Properties();
    classpathProperties.setProperty("avl.enforcement", "block");

    assertThatThrownBy(() -> AvlConfig.load(new Properties(), classpathProperties))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("avl.enforcement");
  }

  @Test
  void getInstanceCachesTheConfigurationForTheJvm() {
    AvlConfig first = AvlConfig.getInstance();
    AvlConfig second = AvlConfig.getInstance();

    assertThat(first).isSameAs(second);
  }

  @Test
  void loadsPropertiesFromAnArbitraryClasspath(@TempDir Path tempDir) throws IOException {
    Path propertiesFile = tempDir.resolve("allure.properties");
    Files.writeString(propertiesFile, "avl.enforcement=fail\n");

    try (URLClassLoader classLoader =
        new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, null)) {
      Properties properties = AvlConfig.loadClasspathProperties(classLoader);

      assertThat(properties.getProperty("avl.enforcement")).isEqualTo("fail");
    }
  }

  @Test
  void returnsEmptyPropertiesWhenNoClasspathResourceExists(@TempDir Path tempDir)
      throws IOException {
    try (URLClassLoader classLoader =
        new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, null)) {
      Properties properties = AvlConfig.loadClasspathProperties(classLoader);

      assertThat(properties).isEmpty();
    }
  }
}
