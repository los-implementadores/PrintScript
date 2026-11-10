package org.printscript.interpreter.semantic;

import org.printscript.source.Position;
import org.printscript.source.PrintScriptException;

/** Programa sintácticamente válido pero con un error de tipos o de declaración. */
public class SemanticException extends PrintScriptException {

  /**
   * @param detail descripción del error, sin la posición
   * @param position posición del nodo que lo produjo
   */
  public SemanticException(String detail, Position position) {
    super("Semantic Error at " + position + ": " + detail, position);
  }
}
