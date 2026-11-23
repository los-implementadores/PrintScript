package org.printscript.analyzer.rule;

import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.CallExpression;

/**
 * Exige que el argumento de una función sea un literal o una variable, no una expresión. Se usa
 * para {@code println} y para {@code readInput}.
 */
public final class SimpleArgumentRule implements NodeRule<CallExpression> {

  private final String function;

  /**
   * @param function nombre de la función a controlar (ej. {@code "println"})
   */
  public SimpleArgumentRule(String function) {
    this.function = function;
  }

  @Override
  public Class<CallExpression> nodeType() {
    return CallExpression.class;
  }

  @Override
  public List<Violation> check(CallExpression call) {
    boolean simple =
        !function.equals(call.getCallee())
            || call.getArguments().isEmpty()
            || call.getArguments().get(0).accept(LiteralOrVariable.INSTANCE);
    if (simple) {
      return List.of();
    }
    return List.of(
        new Violation(
            "Complex expression in '"
                + function
                + "'. Consider extracting to an intermediate variable.",
            call.getPosition()));
  }
}
