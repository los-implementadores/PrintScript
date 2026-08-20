package org.printscript.parser;

import org.printscript.common.Position;

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
public class ParseException extends RuntimeException {

  private final Position position;

  /**
   * @param message descripción del error (qué se esperaba y qué se encontró)
   * @param position posición en el código fuente donde ocurrió el error
   */
  public ParseException(String message, Position position) {
    super(message + " at " + position);
    this.position = position;
  }

  /** Posición en el código fuente donde ocurrió el error sintáctico. */
  public Position getPosition() {
    return position;
  }
}
