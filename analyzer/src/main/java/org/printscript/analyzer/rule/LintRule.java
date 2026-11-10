package org.printscript.analyzer.rule;

import java.util.function.Consumer;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;

/**
 * Una regla del linter.
 *
 * <p>El linter recorre el árbol una sola vez y le avisa a cada regla cada nodo al entrar y al
 * salir. La regla mira los nodos que le interesan ({@code if (node instanceof CallExpression
 * call)}) e ignora el resto, así un tipo de nodo nuevo en el lenguaje no la obliga a cambiar.
 * Reporta lo que encuentre llamando a {@code report}. Todos los métodos tienen una implementación
 * vacía.
 *
 * <p>Agregar una regla es escribir una clase que implemente esta interfaz y registrarla con su
 * clave en {@link LintRules}; el recorrido no se toca.
 */
public interface LintRule {

  /** Se llama al llegar a un nodo, antes de recorrer sus hijos. */
  default void onEnter(Node node, Consumer<Violation> report) {}

  /** Se llama al terminar con un nodo, después de recorrer sus hijos. */
  default void onExit(Node node, Consumer<Violation> report) {}

  /**
   * Se llama al pedir el reporte, para las reglas que necesitan haber visto todo el programa (por
   * ejemplo, variables sin usar). Puede llamarse más de una vez, así que no debe cambiar el estado
   * de la regla.
   */
  default void onFinish(Consumer<Violation> report) {}
}
