package org.printscript.common.ast;

import java.util.Iterator;

/**
 * Nodo raíz del AST: representa el programa completo como una secuencia de sentencias.
 *
 * <p><strong>Contrato:</strong> un {@code Program} se recorre <em>una sola vez</em> con {@link
 * #stream()}. Es el contrato que puede cumplir un programa que se parsea a medida que se lee, sin
 * cargarlo en memoria. Quien necesite varias pasadas tiene que guardarse las sentencias él mismo;
 * que una implementación permita más de un recorrido es un detalle suyo, no algo en lo que se pueda
 * apoyar el consumidor.
 *
 * <p>Implementaciones:
 *
 * <ul>
 *   <li>{@link LazyProgram} — envuelve el parser y produce las sentencias a medida que se piden. Es
 *       la que se usa al procesar código fuente.
 *   <li>{@link EagerProgram} — envuelve una lista ya armada. Útil en tests.
 * </ul>
 */
public interface Program extends Node {

  /**
   * Devuelve un iterator sobre las sentencias del programa, en el orden del código fuente.
   *
   * @throws IllegalStateException si la implementación no admite otro recorrido
   */
  Iterator<Statement> stream();
}
