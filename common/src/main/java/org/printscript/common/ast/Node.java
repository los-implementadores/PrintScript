package org.printscript.common.ast;

import org.printscript.common.Position;

/**
 * Nodo base del AST de PrintScript.
 *
 * <p>Todo nodo conoce su posición en el código fuente para que los mensajes de error puedan incluir
 * fila y columna.
 */
public interface Node {

  /** Posición en el código fuente que abarca este nodo. */
  Position getPosition();

  /** Acepta un visitor para recorrer el árbol. */
  <T> T accept(ASTVisitor<T> visitor);
}
