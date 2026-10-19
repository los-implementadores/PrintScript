package org.printscript.common;

/**
 * Error en el programa del usuario (léxico, sintáctico, semántico o de ejecución), con la posición
 * donde ocurrió.
 *
 * <p>Separa los errores del código fuente de las fallas internas de la implementación: quien
 * orquesta (la CLI, una API) puede atrapar este tipo para reportarlo al usuario y tratar cualquier
 * otra excepción como un bug.
 */
public abstract class PrintScriptException extends RuntimeException {

  private final Position position;

  protected PrintScriptException(String message, Position position) {
    super(message);
    this.position = position;
  }

  /** Posición en el código fuente donde ocurrió el error. */
  public Position getPosition() {
    return position;
  }
}
