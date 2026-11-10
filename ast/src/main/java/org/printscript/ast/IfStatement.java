package org.printscript.ast;

import java.util.ArrayList;
import java.util.List;
import org.printscript.source.Position;

/**
 * Condicional {@code if (cond) { ... } else { ... }}.
 *
 * <ul>
 *   <li>La condición es una expresión que debe evaluar a {@code boolean}.
 *   <li>Cada rama es un {@link Block} (siempre entre llaves en el código fuente).
 *   <li>{@code elseBlock} es {@code null} cuando no hay rama {@code else}. No se soporta {@code
 *       else if}.
 * </ul>
 */
public final class IfStatement implements Statement {

  private final Expression condition;
  private final Block thenBlock;
  private final Block elseBlock;
  private final Position position;

  public IfStatement(Expression condition, Block thenBlock, Block elseBlock, Position position) {
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
    this.position = position;
  }

  public Expression getCondition() {
    return condition;
  }

  public Block getThenBlock() {
    return thenBlock;
  }

  /** Bloque {@code else}, o {@code null} si no hay rama else. */
  public Block getElseBlock() {
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
  public List<Node> children() {
    List<Node> children = new ArrayList<>(List.of(condition, thenBlock));
    if (hasElse()) {
      children.add(elseBlock);
    }
    return children;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitIf(this);
  }
}
