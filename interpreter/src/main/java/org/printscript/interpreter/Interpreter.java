package org.printscript.interpreter;

import org.printscript.ast.Statement;

public interface Interpreter {
  /** Ejecuta una sentencia produciendo sus efectos secundarios. */
  void execute(Statement statement);
}
