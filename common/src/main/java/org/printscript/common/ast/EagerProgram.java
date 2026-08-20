package org.printscript.common.ast;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.printscript.common.Position;

/**
 * Implementación de {@link Program} que materializa todas las sentencias en una lista inmutable en
 * el momento de construcción.
 *
 * <p>Usar cuando se necesita el árbol completo disponible de inmediato, por ejemplo en el analyzer
 * (que puede hacer múltiples pasadas) o en tests.
 *
 * <pre>
 *   Program program = new EagerProgram(List.of(stmt1, stmt2), pos);
 *   program.toList();   // devuelve la lista inmutable directamente
 *   program.stream();   // itera sobre la misma lista
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

  /**
   * Devuelve la lista inmutable de sentencias cargada en construcción. O(1), sin trabajo adicional.
   */
  @Override
  public List<Statement> toList() {
    return statements;
  }

  /** Devuelve un iterator sobre la lista inmutable. No consume memoria adicional. */
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
