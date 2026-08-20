package org.printscript.interpreter;

import org.printscript.common.ast.Statement;

public interface SemanticAnalyzer {
  /** Valida una sentencia. Lanza excepción si es semánticamente inválida. */
  void analyze(Statement statement);
}
