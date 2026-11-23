package org.printscript.analyzer.rule;

import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;

/**
 * Una regla que revisa los nodos de un tipo, de a uno, sin necesitar nada más del programa.
 *
 * <p>El analyzer le pasa solo los nodos de {@link #nodeType()}, ya con ese tipo: la regla no
 * pregunta qué nodo le llegó. Es una función: el mismo nodo da siempre las mismas violaciones, y la
 * misma regla sirve para cualquier cantidad de programas.
 *
 * @param <N> el tipo de nodo que revisa
 */
public interface NodeRule<N extends Node> {

  /** El tipo de nodo que revisa la regla. */
  Class<N> nodeType();

  /** Lo que está mal en {@code node}; vacío si no hay nada. */
  List<Violation> check(N node);
}
