package org.printscript.common.ast;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.printscript.common.Position;

/**
 * Implementación de {@link Program} sobre una lista de sentencias ya armada.
 *
 * <p>Útil en tests, donde el programa se construye a mano. Admite varios recorridos, pero los
 * consumidores de {@link Program} no deben depender de eso (ver el contrato de {@link Program}).
 *
 * <pre>
 *   Program program = new EagerProgram(List.of(stmt1, stmt2), pos);
 *   program.stream();   // itera sobre la lista
 * </pre>
 */
public final class EagerProgram implements Program {

  private final List<Statement> statements;
  private final Position position;

  /**
   * @param statements lista de sentencias (se envuelve en una lista inmutable)
   * @param position posición que abarca el programa completo en el código fuente
   */
  public EagerProgram(List<Statement> statements, Position position) {
    this.statements = Collections.unmodifiableList(statements);
    this.position = position;
  }

  @Override
  public Iterator<Statement> stream() {
    return statements.iterator();
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitProgram(this);
  }
}
