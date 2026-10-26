package org.printscript.analyzer.rule;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.Position;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.VarDeclarationStatement;

/**
 * Reporta las variables declaradas que nunca se leen ni se reasignan. Como necesita haber visto
 * todo el programa, reporta recién en {@link #onFinish}.
 */
public final class UnusedVariableRule implements LintRule {

  /** Variables declaradas, en orden de declaración, con la posición de su declaración. */
  private final Map<String, Position> declared = new LinkedHashMap<>();

  private final Set<String> used = new HashSet<>();

  @Override
  public void onDeclaration(VarDeclarationStatement node, Consumer<Violation> report) {
    declared.putIfAbsent(node.getName().getName(), node.getPosition());
  }

  @Override
  public void onAssignment(AssignmentStatement node, Consumer<Violation> report) {
    markUsed(node.getTarget().getName());
  }

  @Override
  public void onIdentifier(Identifier node, Consumer<Violation> report) {
    markUsed(node.getName());
  }

  @Override
  public void onFinish(Consumer<Violation> report) {
    declared.forEach(
        (name, position) -> {
          if (!used.contains(name)) {
            report.accept(
                new Violation(
                    "Variable '" + name + "' is declared but never used.",
                    Severity.WARNING,
                    position));
          }
        });
  }

  private void markUsed(String name) {
    if (declared.containsKey(name)) {
      used.add(name);
    }
  }
}
