package org.printscript.ast;

import java.util.List;
import org.printscript.source.Position;

/**
 * Nodo base del AST de PrintScript.
 *
 * <p>Todo nodo conoce su posición en el código fuente para que los mensajes de error puedan incluir
 * fila y columna, y sus hijos, para que una herramienta pueda recorrer el árbol sin conocer cada
 * tipo de nodo.
 */
public interface Node {

  /** Posición en el código fuente que abarca este nodo. */
  Position getPosition();

  /**
   * Las expresiones, sentencias y bloques que contiene este nodo, en el orden del código fuente.
   *
   * <p>No incluye el nombre de una declaración ni el destino de una asignación: son nombres que se
   * definen o se escriben, no expresiones que se evalúen. Así, todo {@link Identifier} que aparece
   * al recorrer los hijos es una lectura de una variable.
   *
   * <p>Es lo que permite recorrer el árbol de forma genérica (ver el linter): un nodo nuevo dice
   * cuáles son sus hijos y las herramientas que lo recorren no cambian.
   */
  List<Node> children();

  /** Acepta un visitor para recorrer el árbol. */
  <T> T accept(ASTVisitor<T> visitor);
}
