package org.printscript.interpreter.builtin;

import org.printscript.common.ast.Expression;
import org.printscript.interpreter.ExecutionIo;

/** Lo que el intérprete le ofrece a una {@link BuiltinFunction} para ejecutarse. */
public interface CallContext {

  /**
   * Evalúa una expresión.
   *
   * @param expectedType tipo que espera el contexto mientras se evalúa la expresión
   */
  Object evaluate(Expression expression, String expectedType);

  /** Tipo que espera el contexto de la llamada, o {@code null} si no espera ninguno. */
  String expectedType();

  /** Canales de entrada, entorno y salida de la ejecución. */
  ExecutionIo io();
}
