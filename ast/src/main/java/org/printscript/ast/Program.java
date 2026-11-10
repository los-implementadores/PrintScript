package org.printscript.ast;

import java.util.Iterator;

/**
 * Un programa completo: la secuencia de sentencias del código fuente.
 *
 * <p>No es un nodo del árbol sino la fuente de sus sentencias. <strong>Contrato:</strong> se
 * recorre <em>una sola vez</em> con {@link #stream()}. Es el contrato que puede cumplir un programa
 * que se parsea a medida que se lee, sin cargarlo en memoria. Quien necesite varias pasadas tiene
 * que guardarse las sentencias él mismo; que una implementación permita más de un recorrido es un
 * detalle suyo, no algo en lo que se pueda apoyar el consumidor.
 *
 * <p>Implementaciones:
 *
 * <ul>
 *   <li>{@link LazyProgram} — envuelve el parser y produce las sentencias a medida que se piden. Es
 *       la que se usa al procesar código fuente.
 *   <li>{@link EagerProgram} — envuelve una lista ya armada. Útil en tests.
 * </ul>
 */
public interface Program {

  /**
   * Devuelve un iterator sobre las sentencias del programa, en el orden del código fuente.
   *
   * @throws IllegalStateException si la implementación no admite otro recorrido
   */
  Iterator<Statement> stream();
}
