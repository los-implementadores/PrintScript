package org.printscript.analyzer.rule;

import java.util.function.Consumer;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.Identifier;
import org.printscript.ast.Node;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.StringLiteral;

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
  public void onEnter(Node node, Consumer<Violation> report) {
    if (!(node instanceof CallExpression call)
        || !function.equals(call.getCallee())
        || call.getArguments().isEmpty()) {
      return;
    }
    if (!isLiteralOrIdentifier(call.getArguments().get(0))) {
      report.accept(
          new Violation(
              "Complex expression in '"
                  + function
                  + "'. Consider extracting to an intermediate variable.",
              Severity.WARNING,
              call.getPosition()));
    }
  }

  private static boolean isLiteralOrIdentifier(Expression argument) {
    return argument instanceof NumberLiteral
        || argument instanceof StringLiteral
        || argument instanceof BooleanLiteral
        || argument instanceof Identifier;
  }
}
