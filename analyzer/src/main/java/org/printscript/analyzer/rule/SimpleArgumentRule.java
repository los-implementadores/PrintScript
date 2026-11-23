package org.printscript.analyzer.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.GroupExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.StringLiteral;

/**
 * Exige que los argumentos de una función sean literales o variables, no expresiones. Se usa para
 * {@code println} y para {@code readInput}. Marca el argumento que es una expresión.
 */
public final class SimpleArgumentRule implements NodeRule<CallExpression> {

  /** Los argumentos que no son una expresión: los literales y las variables. */
  private static final Set<Class<? extends Expression>> LITERALS_AND_VARIABLES =
      Set.of(NumberLiteral.class, StringLiteral.class, BooleanLiteral.class, Identifier.class);

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

  /**
   * Si la llamada es a la función que controla la regla, avisa por cada argumento que no es un
   * literal ni una variable.
   */
  @Override
  public List<Violation> onEnter(CallExpression call) {
    if (!callsTheControlledFunction(call)) {
      return List.of();
    }
    List<Violation> violations = new ArrayList<>();
    for (Expression argument : call.getArguments()) {
      if (!isLiteralOrVariable(argument)) {
        violations.add(complexArgument(argument));
      }
    }
    return violations;
  }

  private boolean callsTheControlledFunction(CallExpression call) {
    return function.equals(call.getCallee());
  }

  /** Los paréntesis de afuera no cuentan: {@code (x)} sigue siendo una variable. */
  private static boolean isLiteralOrVariable(Expression argument) {
    return LITERALS_AND_VARIABLES.contains(GroupExpression.unwrap(argument).getClass());
  }

  private Violation complexArgument(Expression argument) {
    return new Violation(
        "Complex expression in '"
            + function
            + "'. Consider extracting to an intermediate variable.",
        argument.getPosition());
  }
}
