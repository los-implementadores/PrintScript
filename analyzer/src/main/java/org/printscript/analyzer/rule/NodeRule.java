package org.printscript.analyzer.rule;

import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;

/**
 * Una regla que revisa los nodos de un tipo, de a uno, sin necesitar nada más del programa.
 *
 * <p>El linter llama a {@link #onEnter} al entrar a cada nodo de tipo {@link #nodeType()}, y se lo
 * pasa ya con ese tipo: la regla no pregunta qué nodo le llegó. No guarda nada: el mismo nodo da
 * siempre las mismas violaciones.
 *
 * @param <N> el tipo de nodo que revisa
 */
public interface NodeRule<N extends Node> {

  /** El tipo de nodo que revisa la regla: el linter le pasa solo nodos de este tipo. */
  Class<N> nodeType();

  /** Lo que está mal en {@code node}, al entrar a él; vacío si no hay nada. */
  List<Violation> onEnter(N node);
}
