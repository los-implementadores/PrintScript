package org.printscript.ast;

import java.util.Iterator;
import java.util.List;

/**
 * Implementación de {@link Program} sobre una lista de sentencias ya armada.
 *
 * <p>Útil en tests, donde el programa se construye a mano. Admite varios recorridos, pero los
 * consumidores de {@link Program} no deben depender de eso (ver el contrato de {@link Program}).
 *
 * <pre>
 *   Program program = new EagerProgram(List.of(stmt1, stmt2));
 *   program.stream();   // itera sobre la lista
 * </pre>
 */
public final class EagerProgram implements Program {

  private final List<Statement> statements;

  /**
   * @param statements lista de sentencias (se copia en una lista inmutable)
   */
  public EagerProgram(List<Statement> statements) {
    this.statements = List.copyOf(statements);
  }

  @Override
  public Iterator<Statement> stream() {
    return statements.iterator();
  }
}
