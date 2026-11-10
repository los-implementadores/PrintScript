package org.printscript.analyzer.rule;

import java.util.Objects;
import java.util.function.Consumer;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;
import org.printscript.ast.VarDeclarationStatement;

/** Exige que los nombres de variables sigan una convención (camelCase o snake_case). */
public final class NamingConventionRule implements LintRule {

  private final NamingConvention convention;

  public NamingConventionRule(NamingConvention convention) {
    this.convention =
        Objects.requireNonNull(
            convention, "La regla de nombres necesita una convención (camelCase o snake_case)");
  }

  @Override
  public void onEnter(Node node, Consumer<Violation> report) {
    if (!(node instanceof VarDeclarationStatement declaration)) {
      return;
    }
    String name = declaration.getName().getName();
    if (!convention.matches(name)) {
      report.accept(
          new Violation(
              "Variable '" + name + "' should be in " + convention.displayName() + ".",
              Severity.WARNING,
              declaration.getPosition()));
    }
  }
}
