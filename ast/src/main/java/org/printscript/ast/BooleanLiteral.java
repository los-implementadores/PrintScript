package org.printscript.ast;

import org.printscript.source.Position;

/** Literal booleano — {@code true} o {@code false}. */
public final class BooleanLiteral implements Expression {

  private final boolean value;
  private final Position position;

  public BooleanLiteral(boolean value, Position position) {
    this.value = value;
    this.position = position;
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitBooleanLiteral(this);
  }
}
