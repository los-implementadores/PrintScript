package org.printscript.lexer;

import org.printscript.common.Position;

/**
 * Error léxico: un caracter o secuencia que no se puede tokenizar (símbolo desconocido, string sin
 * cerrar, etc).
 */
public class LexerException extends RuntimeException {

  private final Position position;

  public LexerException(String message, Position position) {
    super(message + " at " + position);
    this.position = position;
  }

  public Position getPosition() {
    return position;
  }
}
