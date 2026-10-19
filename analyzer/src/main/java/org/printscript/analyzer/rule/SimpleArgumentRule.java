package org.printscript.analyzer.rule;

import java.util.function.Consumer;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.StringLiteral;

/**
 * Exige que el argumento de una función sea un literal o una variable, no una expresión. Se usa
 * para {@code println} y para {@code readInput}.
 */
public final class SimpleArgumentRule implements LintRule {

  private final String function;

  /**
   * @param function nombre de la función a controlar (ej. {@code "println"})
   */
  public SimpleArgumentRule(String function) {
    this.function = function;
  }

  @Override
  public void onCall(CallExpression node, Consumer<Violation> report) {
    if (!function.equals(node.getCallee()) || node.getArguments().isEmpty()) {
      return;
    }
    if (!isLiteralOrIdentifier(node.getArguments().get(0))) {
      report.accept(
          new Violation(
              "Complex expression in '"
                  + function
                  + "'. Consider extracting to an intermediate variable.",
              Severity.WARNING,
              node.getPosition()));
    }
  }

  private static boolean isLiteralOrIdentifier(Expression argument) {
    return argument instanceof NumberLiteral
        || argument instanceof StringLiteral
        || argument instanceof BooleanLiteral
        || argument instanceof Identifier;
  }
}
