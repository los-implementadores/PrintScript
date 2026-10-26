package org.printscript.interpreter.builtin;

import org.printscript.common.Position;
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

  /**
   * Arma el error que lanza la función cuando un argumento no tiene el tipo correcto. La clase del
   * error la decide el análisis semántico, así las funciones no dependen de él.
   */
  RuntimeException error(String detail, Position position);
}
