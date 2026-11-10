package org.printscript.ast;

import java.util.List;
import org.printscript.source.Position;

/**
 * Sentencia compuesta únicamente por una expresión seguida de {@code ;}.
 *
 * <pre>
 *   println(name + " " + lastName);
 * </pre>
 */
public final class ExpressionStatement implements Statement {

  private final Expression expression;
  private final Position position;

  public ExpressionStatement(Expression expression, Position position) {
    this.expression = expression;
    this.position = position;
  }

  public Expression getExpression() {
    return expression;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public List<Node> children() {
    return List.of(expression);
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitExpressionStatement(this);
  }
}
