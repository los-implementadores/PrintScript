package org.printscript.parser;

import org.printscript.common.Position;
import org.printscript.common.PrintScriptException;

/**
 * Excepción lanzada cuando el parser encuentra un token inesperado o una construcción sintáctica
 * inválida.
 *
 * <p>Incluye la {@link Position} del token problemático para que el mensaje de error pueda indicar
 * fila y columna exactas en el código fuente.
 *
 * <p>El parser usa una estrategia fail-fast: lanza esta excepción en cuanto detecta el primer error
 * sintáctico, sin intentar recuperarse.
 */
public class ParseException extends PrintScriptException {

  /**
   * @param message descripción del error (qué se esperaba y qué se encontró)
   * @param position posición en el código fuente donde ocurrió el error
   */
  public ParseException(String message, Position position) {
    super(message + " at " + position, position);
  }
}
