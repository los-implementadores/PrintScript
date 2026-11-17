package org.printscript.formatter.rule;

import org.printscript.ast.CallExpression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.Node;

/** Qué es una sentencia {@code println}, para las reglas de líneas vacías. */
final class PrintlnStatement {

  private PrintlnStatement() {}

  /** {@code true} si {@code node} es una sentencia que llama a {@code println}. */
  static boolean is(Node node) {
    return node instanceof ExpressionStatement statement
        && statement.getExpression() instanceof CallExpression call
        && "println".equals(call.getCallee());
  }
}
