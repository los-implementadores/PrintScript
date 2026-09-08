package org.printscript.common.ast;

import java.util.Collections;
import java.util.List;
import org.printscript.common.Position;

/**
 * Condicional {@code if (cond) { ... } else { ... }} de PrintScript 1.1.
 *
 * <ul>
 *   <li>La condición es una expresión que debe evaluar a {@code boolean}.
 *   <li>Los bloques son listas de sentencias (siempre entre llaves en el código fuente).
 *   <li>{@code elseBlock} es {@code null} cuando no hay rama {@code else}. No se soporta {@code
 *       else if}.
 * </ul>
 */
public final class IfStatement implements Statement {

  private final Expression condition;
  private final List<Statement> thenBlock;
  private final List<Statement> elseBlock;
  private final Position position;

  public IfStatement(
      Expression condition,
      List<Statement> thenBlock,
      List<Statement> elseBlock,
      Position position) {
    this.condition = condition;
    this.thenBlock = Collections.unmodifiableList(thenBlock);
    this.elseBlock = elseBlock == null ? null : Collections.unmodifiableList(elseBlock);
    this.position = position;
  }

  public Expression getCondition() {
    return condition;
  }

  public List<Statement> getThenBlock() {
    return thenBlock;
  }

  /** Bloque {@code else}, o {@code null} si no hay rama else. */
  public List<Statement> getElseBlock() {
    return elseBlock;
  }

  public boolean hasElse() {
    return elseBlock != null;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitIf(this);
  }
}
