package org.printscript.interpreter.builtin;

import org.printscript.common.ast.Expression;

/** Lo que el análisis semántico le ofrece a una {@link BuiltinFunction} para chequear tipos. */
public interface TypeChecker {

  /**
   * Devuelve el tipo de una expresión.
   *
   * @param expectedType tipo que espera el contexto mientras se analiza la expresión
   */
  String typeOf(Expression expression, String expectedType);

  /** Tipo que espera el contexto de la llamada, o {@code null} si no espera ninguno. */
  String expectedType();
}
