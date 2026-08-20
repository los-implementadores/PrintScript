package org.printscript.common.ast;

import org.printscript.common.Position;

/** Literal de cadena — p.ej. {@code "hello"}. */
public final class StringLiteral implements Expression {

  private final String value;
  private final Position position;

  public StringLiteral(String value, Position position) {
    this.value = value;
    this.position = position;
  }

  public String getValue() {
    return value;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitStringLiteral(this);
  }
}
