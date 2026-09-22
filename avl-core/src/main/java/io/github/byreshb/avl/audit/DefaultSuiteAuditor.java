package io.github.byreshb.avl.audit;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Default {@link SuiteAuditor} implementation, backed by {@code javaparser-symbol-solver-core} for
 * parsing (source-level annotation lookup only; no classpath-based type resolution is required for
 * the four label annotations, which are always used by their simple name).
 */
public final class DefaultSuiteAuditor implements SuiteAuditor {

  private static final String JUNIT_TEST_ANNOTATION = "org.junit.jupiter.api.Test";
  private static final String TESTNG_TEST_ANNOTATION = "org.testng.annotations.Test";

  private static final Map<String, String> LABEL_ANNOTATIONS =
      Map.of(
          "Layer", "layer",
          "Team", "team",
          "Priority", "priority",
          "Component", "component");

  @Override
  public AuditReport audit(Path sourceRoot, Set<String> requiredLabels) {
    List<TestFinding> findings = new ArrayList<>();
    for (Path javaFile : listJavaFiles(sourceRoot)) {
      findings.addAll(auditFile(sourceRoot, javaFile, requiredLabels));
    }
    return new AuditReport(findings.size(), requiredLabels, findings);
  }

  private List<Path> listJavaFiles(Path sourceRoot) {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to walk " + sourceRoot, e);
    }
  }

  private List<TestFinding> auditFile(Path sourceRoot, Path javaFile, Set<String> requiredLabels) {
    CompilationUnit unit = parse(javaFile);
    Set<String> testAnnotationImports = testAnnotationImports(unit);
    Path relativeFile = sourceRoot.relativize(javaFile);

    List<TestFinding> findings = new ArrayList<>();
    for (ClassOrInterfaceDeclaration type : unit.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = type.getFullyQualifiedName().orElse(type.getNameAsString());
      for (MethodDeclaration method : type.getMethods()) {
        if (!isTestMethod(method, testAnnotationImports)) {
          continue;
        }
        Map<String, String> resolvedLabels = resolveLabels(type, method);
        Set<String> missing = missingRequiredLabels(requiredLabels, resolvedLabels);
        findings.add(
            new TestFinding(
                className, method.getNameAsString(), relativeFile, resolvedLabels, missing));
      }
    }
    return findings;
  }

  private CompilationUnit parse(Path javaFile) {
    try {
      return StaticJavaParser.parse(javaFile);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read " + javaFile, e);
    }
  }

  private Set<String> testAnnotationImports(CompilationUnit unit) {
    Set<String> imports = new LinkedHashSet<>();
    for (ImportDeclaration importDeclaration : unit.getImports()) {
      String name = importDeclaration.getNameAsString();
      if (importDeclaration.isAsterisk()) {
        if (name.equals("org.junit.jupiter.api") || name.equals("org.testng.annotations")) {
          imports.add(name + ".Test");
        }
      } else if (name.equals(JUNIT_TEST_ANNOTATION) || name.equals(TESTNG_TEST_ANNOTATION)) {
        imports.add(name);
      }
    }
    return imports;
  }

  private boolean isTestMethod(MethodDeclaration method, Set<String> testAnnotationImports) {
    return method.getAnnotations().stream()
        .anyMatch(a -> isTestAnnotation(a, testAnnotationImports));
  }

  private boolean isTestAnnotation(AnnotationExpr annotation, Set<String> testAnnotationImports) {
    String name = annotation.getNameAsString();
    if (name.equals(JUNIT_TEST_ANNOTATION) || name.equals(TESTNG_TEST_ANNOTATION)) {
      return true;
    }
    return name.equals("Test")
        && (testAnnotationImports.contains(JUNIT_TEST_ANNOTATION)
            || testAnnotationImports.contains(TESTNG_TEST_ANNOTATION));
  }

  private Map<String, String> resolveLabels(
      ClassOrInterfaceDeclaration type, MethodDeclaration method) {
    Map<String, String> labels = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : LABEL_ANNOTATIONS.entrySet()) {
      annotationValue(method, entry.getKey())
          .or(() -> annotationValue(type, entry.getKey()))
          .ifPresent(value -> labels.put(entry.getValue(), value));
    }
    return labels;
  }

  private Optional<String> annotationValue(NodeWithAnnotations<?> node, String annotationName) {
    return node.getAnnotations().stream()
        .filter(a -> a.getNameAsString().equals(annotationName))
        .findFirst()
        .flatMap(this::extractValue);
  }

  private Optional<String> extractValue(AnnotationExpr annotation) {
    if (!(annotation instanceof SingleMemberAnnotationExpr single)) {
      return Optional.empty();
    }
    Expression value = single.getMemberValue();
    if (value instanceof StringLiteralExpr string) {
      return Optional.of(string.asString());
    }
    if (value instanceof FieldAccessExpr fieldAccess) {
      return Optional.of(fieldAccess.getNameAsString());
    }
    if (value instanceof NameExpr name) {
      return Optional.of(name.getNameAsString());
    }
    return Optional.empty();
  }

  private Set<String> missingRequiredLabels(
      Set<String> requiredLabels, Map<String, String> resolvedLabels) {
    Set<String> missing = new LinkedHashSet<>();
    for (String required : requiredLabels) {
      if (!resolvedLabels.containsKey(required)) {
        missing.add(required);
      }
    }
    return missing;
  }
}
