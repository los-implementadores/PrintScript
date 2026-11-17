package org.printscript.ast;

import java.util.List;
import org.printscript.source.Position;

/**
 * Expresión entre paréntesis: {@code ( expr )}.
 *
 * <p>Para evaluar no hace falta: la precedencia ya quedó en la forma del árbol. Existe para que el
 * formatter escriba el programa como estaba; sin este nodo, {@code (1 + 2) * 3} se escribiría
 * {@code 1 + 2 * 3}, que es otro programa. Es el mismo motivo por el que el AST de Rust tiene
 * {@code ExprKind::Paren} (ver ADR-029).
 */
public final class GroupExpression implements Expression {

  private final Expression expression;
  private final Position position;

  /**
   * @param expression la expresión de adentro
   * @param position desde el {@code (} hasta el {@code )}
   */
  public GroupExpression(Expression expression, Position position) {
    this.expression = expression;
    this.position = position;
  }

  /** La expresión sin los paréntesis, aunque haya varios anidados: {@code ((x))} da {@code x}. */
  public static Expression unwrap(Expression expression) {
    Expression current = expression;
    while (current instanceof GroupExpression group) {
      current = group.getExpression();
    }
    return current;
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
    return visitor.visitGroup(this);
  }
}
