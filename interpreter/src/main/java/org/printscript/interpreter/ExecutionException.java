package org.printscript.interpreter;

import org.printscript.common.Position;
import org.printscript.common.PrintScriptException;

/**
 * Error que sólo aparece al ejecutar: depende de valores de runtime (división por cero, input que
 * no se puede convertir, variable de entorno inexistente).
 */
public class ExecutionException extends PrintScriptException {

  /**
   * @param detail descripción del error, sin la posición
   * @param position posición del nodo que lo produjo
   */
  public ExecutionException(String detail, Position position) {
    super("Execution Error at " + position + ": " + detail, position);
  }
}
