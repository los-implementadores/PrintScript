package org.printscript.interpreter;

import org.printscript.common.ast.Statement;

public interface Interpreter {
  /** Ejecuta una sentencia produciendo sus efectos secundarios. */
  void execute(Statement statement);
}
