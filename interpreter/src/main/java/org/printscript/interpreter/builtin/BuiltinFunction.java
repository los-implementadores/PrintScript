package org.printscript.interpreter.builtin;

import org.printscript.common.ast.CallExpression;

/**
 * Función nativa del lenguaje ({@code println}, {@code readInput}, {@code readEnv}).
 *
 * <p>Concentra todo lo que el lenguaje sabe de la función: cuántos argumentos recibe, cómo se
 * chequean sus tipos y cómo se ejecuta. El análisis semántico y el intérprete no la conocen por
 * nombre: la buscan en {@link BuiltinFunctions}. Qué funciones existen lo decide quien arma el
 * {@link BuiltinFunctions}.
 */
public interface BuiltinFunction {

  /** Nombre con el que se la llama en el código fuente. */
  String name();

  /** Cantidad exacta de argumentos que recibe. */
  int arity();

  /**
   * Chequea los tipos de los argumentos. La aridad ya fue validada.
   *
   * @return el tipo del resultado, o {@code null} si la función no devuelve valor
   * @throws RuntimeException el error que arma {@link TypeChecker#error} si un argumento es
   *     inválido
   */
  String checkTypes(TypeChecker checker, CallExpression call);

  /**
   * Ejecuta la llamada. La aridad ya fue validada.
   *
   * @return el valor del resultado, o {@code null} si la función no devuelve valor
   * @throws org.printscript.interpreter.ExecutionException si falla en runtime
   */
  Object call(CallContext context, CallExpression call);
}
