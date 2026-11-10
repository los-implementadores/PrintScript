package org.printscript.lexer;

import org.printscript.source.Position;
import org.printscript.source.PrintScriptException;

/**
 * Error léxico: un caracter o secuencia que no se puede tokenizar (símbolo desconocido, string sin
 * cerrar, etc).
 */
public class LexerException extends PrintScriptException {

  public LexerException(String message, Position position) {
    super(message + " at " + position, position);
  }
}
