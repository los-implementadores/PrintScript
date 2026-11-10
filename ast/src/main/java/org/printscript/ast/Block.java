package org.printscript.ast;

import java.util.List;
import org.printscript.source.Position;

/**
 * Bloque de sentencias entre llaves: {@code { ... }}.
 *
 * <p>Abre un scope: lo que se declara adentro deja de existir al salir. Lo usan las ramas del
 * {@link IfStatement}, y lo puede reusar cualquier construcción nueva que tenga un cuerpo.
 */
public final class Block implements Node {

  private final List<Statement> statements;
  private final Position position;

  public Block(List<Statement> statements, Position position) {
    this.statements = List.copyOf(statements);
    this.position = position;
  }

  /** Sentencias del bloque, en orden. */
  public List<Statement> getStatements() {
    return statements;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public List<Node> children() {
    return List.copyOf(statements);
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitBlock(this);
  }
}
